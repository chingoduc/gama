package ummisco.gama.modernOpenGL;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;

import msi.gama.outputs.LightPropertiesStructure;
import ummisco.gama.modernOpenGL.shader.ShaderProgram;
import ummisco.gama.opengl.ModernRenderer;
import ummisco.gama.opengl.vaoGenerator.TransformationMatrix;

public class ModernDrawer {
	
	private Matrix4f transformationMatrix;

	int[] vboHandles;
	HashMap<String,ArrayList<ArrayList<DrawingEntity>>> mapEntities = new HashMap<String,ArrayList<ArrayList<DrawingEntity>>>();
	final ModernRenderer renderer;
	GL2 gl;
	
	ArrayList<Integer> listOfVAOUsed = new ArrayList<Integer>();
	
	ArrayList<int[]> typeOfDrawingBuffer = new ArrayList<int[]>(); 	// this variable will store the types of drawing with the place
																	// in the buffer, so that all objects can be in the same VBO :
																	// int[0] : type of drawing ("GL2.GL_POINTS" / "GL2.GL_TRIANGLES"...)
																	// int[1] : the position in the draw buffer
																	// int[2] : the length of the element
	HashMap<Integer,Integer> currentPositionInBuffer = new HashMap<Integer,Integer>();
	int idxBuffPositionInVBO = 0;
	
	static final int COLOR_IDX = 0;
	static final int VERTICES_IDX = 1;
	static final int IDX_BUFF_IDX = 2;
	static final int NORMAL_IDX = 3;
	static final int UVMAPPING_IDX = 4;
	
	public ModernDrawer(ModernRenderer renderer, GL2 gl) {
		this.renderer = renderer;
		this.gl = gl;
		
		vboHandles = new int[5];
		this.gl.glGenBuffers(5, vboHandles, 0);
		
		// init map
		mapEntities.put(DrawingEntity.Type.LINE.toString(), null);
		mapEntities.put(DrawingEntity.Type.FACE.toString(), null);
		mapEntities.put(DrawingEntity.Type.TEXTURED.toString(), null);
	}
	
	public void addDrawingEntities(DrawingEntity[] entities) {
		for (DrawingEntity entity : entities) {
			if (entity.type.equals(DrawingEntity.Type.LINE)) {
				addLineEntity(entity);
			}
			else if (entity.type.equals(DrawingEntity.Type.FACE)) {
				addFilledEntity(entity);
			}
			else if (entity.type.equals(DrawingEntity.Type.TEXTURED)) {
				addTexturedEntity(entity);
			}
			else if (entity.type.equals(DrawingEntity.Type.POINT)) {
				addPointEntity(entity);
			}
		}
	}
	
	public void addLineEntity(DrawingEntity newEntity) {
		// all the line entities are using the same shader. We have to put them all together
		ArrayList<ArrayList<DrawingEntity>> lineEntities = mapEntities.get(DrawingEntity.Type.LINE.toString());
		ArrayList<ArrayList<DrawingEntity>> listToAdd = new ArrayList<ArrayList<DrawingEntity>>();
		if (lineEntities == null) {
			ArrayList<DrawingEntity> entityList = new ArrayList<DrawingEntity>();
			entityList.add(newEntity);
			listToAdd.add(entityList);
		}
		else {
			listToAdd = lineEntities;
			listToAdd.get(0).add(newEntity);
		}
		mapEntities.put(DrawingEntity.Type.LINE.toString(), listToAdd);
	}
	
	public void addPointEntity(DrawingEntity newEntity) {
		// all the point entities are using the same shader. We have to put them all together
		ArrayList<ArrayList<DrawingEntity>> pointEntities = mapEntities.get(DrawingEntity.Type.POINT.toString());
		ArrayList<ArrayList<DrawingEntity>> listToAdd = new ArrayList<ArrayList<DrawingEntity>>();
		if (pointEntities == null) {
			ArrayList<DrawingEntity> entityList = new ArrayList<DrawingEntity>();
			entityList.add(newEntity);
			listToAdd.add(entityList);
		}
		else {
			listToAdd = pointEntities;
			listToAdd.get(0).add(newEntity);
		}
		mapEntities.put(DrawingEntity.Type.POINT.toString(), listToAdd);
	}
	
	public void addFilledEntity(DrawingEntity newEntity) {
		ArrayList<ArrayList<DrawingEntity>> filledEntities = mapEntities.get(DrawingEntity.Type.FACE.toString());
		ArrayList<ArrayList<DrawingEntity>> listToAdd = new ArrayList<ArrayList<DrawingEntity>>();
		if (filledEntities == null) {
			ArrayList<DrawingEntity> entityList = new ArrayList<DrawingEntity>();
			entityList.add(newEntity);
			listToAdd.add(entityList);
		}
		else {
			listToAdd = filledEntities;
			// add to the entities with the same material
			boolean entityAdded = false;
			for (int i = 0 ; i < filledEntities.size() ; i++) {
				DrawingEntity entity = filledEntities.get(i).get(0);
				if (entity.getMaterial().equalsTo(newEntity.getMaterial())) {
					// same material --> we concatenate newEntity with the other entities with the same material
					listToAdd.get(i).add(newEntity);
					// we change the value of the flag
					entityAdded = true;
				}
			}
			if (!entityAdded) {
				// the material of newEntity has not been added yet. Create a new entity
				ArrayList<DrawingEntity> entityList = new ArrayList<DrawingEntity>();
				entityList.add(newEntity);
				listToAdd.add(entityList);
			}
		}
		mapEntities.put(DrawingEntity.Type.FACE.toString(), listToAdd);
	}
	
	public void addTexturedEntity(DrawingEntity newEntity) {
		ArrayList<ArrayList<DrawingEntity>> texturedEntities = mapEntities.get(DrawingEntity.Type.TEXTURED.toString());
		ArrayList<ArrayList<DrawingEntity>> listToAdd = new ArrayList<ArrayList<DrawingEntity>>();
		if (texturedEntities == null) {
			ArrayList<DrawingEntity> entityList = new ArrayList<DrawingEntity>();
			entityList.add(newEntity);
			listToAdd.add(entityList);
		}
		else {
			listToAdd = texturedEntities;
			// add to the entities with the same material
			boolean entityAdded = false;
			for (int i = 0 ; i < texturedEntities.size() ; i++) {
				DrawingEntity entity = texturedEntities.get(i).get(0);
				if (entity.getMaterial().equalsTo(newEntity.getMaterial()) 
						&& entity.getTextureID() == newEntity.getTextureID()) {
					// same material --> we concatenate newEntity with the other entities with the same material
					listToAdd.get(i).add(newEntity);
					// we change the value of the flag
					entityAdded = true;
				}
			}
			if (!entityAdded) {
				// the material of newEntity has not been added yet. Create a new entity
				ArrayList<DrawingEntity> entityList = new ArrayList<DrawingEntity>();
				entityList.add(newEntity);
				listToAdd.add(entityList);
			}
		}
		mapEntities.put(DrawingEntity.Type.TEXTURED.toString(), listToAdd);
	}
	
	public void clearVBO() {
		for (Integer vao : listOfVAOUsed) {
			gl.glDisableVertexAttribArray(vao);
		}
		listOfVAOUsed.clear();
		typeOfDrawingBuffer.clear();
		currentPositionInBuffer.clear();
		
		// reinit currentPositionInBuffer
		currentPositionInBuffer.put(ShaderProgram.COLOR_ATTRIBUTE_IDX, 0);
		currentPositionInBuffer.put(ShaderProgram.NORMAL_ATTRIBUTE_IDX, 0);
		currentPositionInBuffer.put(ShaderProgram.POSITION_ATTRIBUTE_IDX, 0);
		currentPositionInBuffer.put(ShaderProgram.UVMAPPING_ATTRIBUTE_IDX, 0);
		idxBuffPositionInVBO = 0;
	}
	
	public void draw() {
		
//		if (!flag) {
//			simpleDrawMethod();
//		}
//		else {
		clearVBO();
		for (String key : mapEntities.keySet()) {
			ArrayList<ArrayList<DrawingEntity>> listOfListOfEntities = mapEntities.get(key);
			if (listOfListOfEntities != null) {
				for (ArrayList<DrawingEntity> listOfEntities : listOfListOfEntities) {
					// all those entities are using the same shader
					ShaderProgram shaderProgram = new ShaderProgram(gl);
					shaderProgram.start();
						
					prepareShader(listOfEntities.get(0),key,shaderProgram);
					
					drawLights(renderer.data.getDiffuseLights());
					loadVBO(listOfEntities,key);
					
					drawVBO();
					clearVBO();
					
					shaderProgram.stop();
				}
			}
		}
		
		mapEntities.clear();
		
	}
	
	private void drawLights(List<LightPropertiesStructure> lights) {
		for (LightPropertiesStructure light : lights) {
			if (light.isDrawLight()) {
				
			}
		}
	}
	
	private boolean useNormals(String drawingType) {
		if (drawingType.equals(DrawingEntity.Type.LINE.toString())
				|| drawingType.equals(DrawingEntity.Type.POINT.toString()))
			return false;
		return true;
	}
	
	private void drawVBO() {
		for (int[] elem : typeOfDrawingBuffer) {
			gl.glDrawElements(elem[0], elem[2], GL2.GL_UNSIGNED_INT, elem[1]);
		}
	}
	
	private void prepareShader(DrawingEntity entity, String drawingType, ShaderProgram shaderProgram) {
		transformationMatrix = TransformationMatrix.createTransformationMatrix(new Vector3f(0,0,0), 0, 0, 0, 1);
		shaderProgram.loadTransformationMatrix(transformationMatrix);
		shaderProgram.loadViewMatrix(renderer.camera);
		shaderProgram.loadProjectionMatrix(renderer.getProjectionMatrix());
		
		shaderProgram.loadAmbientLight(new Vector3f(
				(float) renderer.data.getAmbientLightColor().getRed() / 255f,
				(float) renderer.data.getAmbientLightColor().getGreen() / 255f,
				(float) renderer.data.getAmbientLightColor().getBlue() / 255f));
		shaderProgram.loadDiffuseLights(renderer.data.getDiffuseLights());
		boolean useNormals = useNormals(drawingType);
		if (useNormals) {
			shaderProgram.enableNormal();
			float shineDamper = (float) entity.getMaterial().getShineDamper();
			float reflectivity = (float) entity.getMaterial().getReflectivity();
			shaderProgram.loadShineVariables(shineDamper,reflectivity);
		}
		else {
			shaderProgram.disableNormal();
		}
		
		if (entity.getUvMapping() == null) {
			shaderProgram.disableTexture();
		}
		else {
			shaderProgram.enableTexture();
			shaderProgram.loadTexture(0);
		}
	}
	
	private void loadVBO(ArrayList<DrawingEntity> listEntities, String drawingType) {
		
		ArrayList<float[]> listVertices = new ArrayList<float[]>();
		ArrayList<float[]> listColors = new ArrayList<float[]>();
		ArrayList<float[]> listIdxBuffer = new ArrayList<float[]>();
		ArrayList<float[]> listNormals = new ArrayList<float[]>();
		ArrayList<float[]> listUvMapping = new ArrayList<float[]>();
		for (DrawingEntity entity : listEntities) {
			listVertices.add(entity.getVertices());
			listColors.add(entity.getColors());
			listIdxBuffer.add(entity.getIndices());
			listNormals.add(entity.getNormals());
			if (entity.getUvMapping() != null)
				listUvMapping.add(entity.getUvMapping());
		}


		// VERTICES POSITIONS BUFFER
		storeDataInAttributeList(ShaderProgram.POSITION_ATTRIBUTE_IDX,VERTICES_IDX,listVertices);
		
		// COLORS BUFFER (If no texture is defined)
		if (listUvMapping.size() == 0) {
			storeDataInAttributeList(ShaderProgram.COLOR_ATTRIBUTE_IDX,COLOR_IDX,listColors);
		}
		
		// UV MAPPING (If a texture is defined)
		else {
			storeDataInAttributeList(ShaderProgram.UVMAPPING_ATTRIBUTE_IDX,UVMAPPING_IDX,listUvMapping);
			gl.glActiveTexture(GL.GL_TEXTURE0);
			gl.glBindTexture(GL.GL_TEXTURE_2D, listEntities.get(0).getTextureID());
		}
		
		// NORMAL BUFFER
		if (useNormals(drawingType))
			storeDataInAttributeList(ShaderProgram.NORMAL_ATTRIBUTE_IDX,NORMAL_IDX,listNormals);
		
		// INDEX BUFFER
		int sizeIdxBuffer = 0;
		for (float[] idxBuffer : listIdxBuffer) {
			sizeIdxBuffer += idxBuffer.length;
		}
		int[] intIdxBuffer = new int[sizeIdxBuffer];
		
		int cpt = 0;
		int offset = 0;
		for (int i = 0 ; i < listIdxBuffer.size() ; i++) {
			float[] idxBuffer = listIdxBuffer.get(i);
			int maxIdx = 0;
			for (int j = 0 ; j < idxBuffer.length ; j++) {
				if ((int)idxBuffer[j]>maxIdx) {maxIdx = (int) idxBuffer[j];}
				intIdxBuffer[offset+j] = (int) idxBuffer[j] + cpt ;
			}
			offset += idxBuffer.length;
			cpt += maxIdx+1;
		}
		IntBuffer ibIdxBuff = Buffers.newDirectIntBuffer(intIdxBuffer);
		// Select the VBO, GPU memory data, to use for colors
		gl.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, vboHandles[IDX_BUFF_IDX]);
		int numBytes = intIdxBuffer.length * 4;
		gl.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, numBytes, ibIdxBuff, GL2.GL_STATIC_DRAW);
		ibIdxBuff.rewind();

		int[] newElement = new int[3];
		if (drawingType.equals(DrawingEntity.Type.POINT.toString())) {
			// particular case : drawing just a point
			newElement[0] = GL2.GL_POINTS;
			//gl.glDrawElements(GL2.GL_POINTS, idxBuffer.length, GL2.GL_UNSIGNED_INT, 0);
		}
		else if (drawingType.equals(DrawingEntity.Type.LINE.toString())) {
			// draw border (lines)
			newElement[0] = GL2.GL_LINES;
			//gl.glDrawElements(GL2.GL_LINES, idxBuffer.length, GL2.GL_UNSIGNED_INT, 0);
		}
		else {
			// draw triangles
			newElement[0] = GL2.GL_TRIANGLES;
			//gl.glDrawElements(GL2.GL_TRIANGLES, idxBuffer.length, GL2.GL_UNSIGNED_INT, 0);
		}
		newElement[1] = idxBuffPositionInVBO;
		newElement[2] = intIdxBuffer.length;
		typeOfDrawingBuffer.add(newElement);
		idxBuffPositionInVBO += intIdxBuffer.length;

		//releaseVAOMemory();
	}
	
	private void storeDataInAttributeList(int shaderAttributeNumber, int bufferAttributeNumber, ArrayList<float[]> listData) {
		int coordinateSize = 0;
		switch (shaderAttributeNumber) {
		// recognize the type of VAO to determine the size of the coordinates
			case ShaderProgram.COLOR_ATTRIBUTE_IDX : coordinateSize = 4; break; // r, g, b, a
			case ShaderProgram.POSITION_ATTRIBUTE_IDX : coordinateSize = 3; break; // x, y, z
			case ShaderProgram.NORMAL_ATTRIBUTE_IDX : coordinateSize = 3; break; // x, y, z
			case ShaderProgram.UVMAPPING_ATTRIBUTE_IDX : coordinateSize = 2; break; // u, v
		}
		// Select the VBO, GPU memory data, to use for data
		gl.glBindBuffer(GL2.GL_ARRAY_BUFFER, vboHandles[bufferAttributeNumber]);
		// compute the total size of the buffer :
		int numBytes = 0;
		for (float[] data : listData) {
			numBytes += data.length * 4;
		}
		gl.glBufferData(GL2.GL_ARRAY_BUFFER, numBytes, null, GL2.GL_STATIC_DRAW);
		
		int offset = 0;
		for (float[] data : listData)
		{
			FloatBuffer fbData = Buffers.newDirectFloatBuffer(data/*totalData,positionInBuffer*/);
			gl.glBufferSubData(GL2.GL_ARRAY_BUFFER, offset, data.length*4, fbData);
			offset += data.length*4;
			//fbData.rewind(); // It is OK to release CPU after transfer to GPU
		}
		
		// Associate Vertex attribute with the last bound VBO
		gl.glVertexAttribPointer(shaderAttributeNumber, coordinateSize,
		                    GL2.GL_FLOAT, false /* normalized? */, 0 /* stride */,
		                    0 /* The bound VBO data offset */);
		gl.glEnableVertexAttribArray(shaderAttributeNumber);
		if (!listOfVAOUsed.contains(shaderAttributeNumber))
		{
			listOfVAOUsed.add(shaderAttributeNumber);
		}
	}

}
