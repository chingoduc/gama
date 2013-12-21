package simtools.gaml.extensions.traffic;

import java.util.List;
import msi.gama.common.interfaces.IKeyword;
import msi.gama.common.util.GeometryUtils;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.*;
import msi.gama.metamodel.topology.ITopology;
import msi.gama.metamodel.topology.filter.In;
import msi.gama.precompiler.GamlAnnotations.action;
import msi.gama.precompiler.GamlAnnotations.arg;
import msi.gama.precompiler.GamlAnnotations.doc;
import msi.gama.precompiler.GamlAnnotations.getter;
import msi.gama.precompiler.GamlAnnotations.setter;
import msi.gama.precompiler.GamlAnnotations.skill;
import msi.gama.precompiler.GamlAnnotations.var;
import msi.gama.precompiler.GamlAnnotations.vars;
import msi.gama.runtime.GAMA;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.*;
import msi.gama.util.graph.GamaGraph;
import msi.gama.util.path.*;
import msi.gaml.operators.Spatial.Operators;
import msi.gaml.operators.Spatial.Punctal;
import msi.gaml.operators.Spatial.Transformations;
import msi.gaml.skills.MovingSkill;
import msi.gaml.types.*;

import com.vividsolutions.jts.geom.*;

@vars({
	@var(name = IKeyword.SPEED, type = IType.FLOAT, init = "1.0", doc = @doc("the speed of the agent (in meter/second)")),
	@var(name = "security_distance", type = IType.FLOAT, init = "1.0", doc = @doc("the min distance between two drivers")),
	@var(name = "real_speed", type = IType.FLOAT, init = "0.0", doc = @doc("real speed of the agent (in meter/second)")),
	@var(name = "current_lane", type = IType.INT, init = "0", doc = @doc("the current lane on which the agent is")),
	@var(name = "vehicle_length", type = IType.FLOAT, init = "0.0", doc = @doc("the length of the agent geometry")),
	@var(name = "current_road", type = IType.AGENT, doc = @doc("current road on which the agent is")),
	@var(name = "proba_lane_change_up", type = IType.FLOAT,init = "1.0",doc = @doc("probability to change lane to a upper lane (left lane if right side driving) if necessary")),
	@var(name = "proba_lane_change_down", type = IType.FLOAT,init = "1.0",doc = @doc("probability to change lane to a lower lane (right lane if right side driving) if necessary")),
	@var(name = "right_side_driving", type = IType.BOOL, init = "true",doc = @doc("have drivers to drive on the right size of the road?"))})
@skill(name = "advanced_driving")
public class AdvancedDrivingSkill extends MovingSkill {

	public final static String SECURITY_DISTANCE = "security_distance";
	public final static String REAL_SPEED = "real_speed";
	public final static String CURRENT_ROAD = "current_road";
	public final static String CURRENT_LANE = "current_lane";
	public final static String DISTANCE_TO_GOAL = "distance_to_goal";
	public final static String VEHICLE_LENGTH = "vehicle_length";
	public final static String PROBA_LANE_CHANGE_UP = "proba_lane_change_up";
	public final static String PROBA_LANE_CHANGE_DOWN = "proba_lane_change_down";
	public final static String RIGHT_SIDE_DRIVING = "right_side_driving";
	
	@getter(PROBA_LANE_CHANGE_DOWN)
	public double getProbaLaneChangeDown(final IAgent agent) {
		return (Double) agent.getAttribute(PROBA_LANE_CHANGE_DOWN);
	}
	@setter(PROBA_LANE_CHANGE_DOWN)
	public void setProbaLaneChangeDown(final IAgent agent, final Double proba) {
		agent.setAttribute(PROBA_LANE_CHANGE_DOWN, proba);
	}
	@getter(PROBA_LANE_CHANGE_UP)
	public double getProbaLaneChangeUp(final IAgent agent) {
		return (Double) agent.getAttribute(PROBA_LANE_CHANGE_UP);
	}
	@setter(PROBA_LANE_CHANGE_UP)
	public void setProbaLaneChangeUp(final IAgent agent, final Double proba) {
		agent.setAttribute(PROBA_LANE_CHANGE_UP, proba);
	}
	@getter(RIGHT_SIDE_DRIVING)
	public boolean getRightSideDriving(final IAgent agent) {
		return (Boolean) agent.getAttribute(RIGHT_SIDE_DRIVING);
	}
	@setter(RIGHT_SIDE_DRIVING)
	public void setRightSideDriving(final IAgent agent, final Boolean isRight) {
		agent.setAttribute(RIGHT_SIDE_DRIVING, isRight);
	}

	@getter(SECURITY_DISTANCE)
	public double getSecurityDistance(final IAgent agent) {
		return (Double) agent.getAttribute(SECURITY_DISTANCE);
	}

	@setter(SECURITY_DISTANCE)
	public void setSecurityDistance(final IAgent agent, final double ls) {
		agent.setAttribute(SECURITY_DISTANCE, ls);
	}

	@getter(CURRENT_ROAD)
	public IAgent getCurrentRoad(final IAgent agent) {
		return (IAgent) agent.getAttribute(CURRENT_ROAD);
	}
	
	@getter(REAL_SPEED)
	public double getRealSpeed(final IAgent agent) {
		return (Double) agent.getAttribute(REAL_SPEED);
	}
	
	@getter(VEHICLE_LENGTH)
	public double getVehiculeLength(final IAgent agent) {
		return (Double) agent.getAttribute(VEHICLE_LENGTH);
	}
	
	@getter(CURRENT_LANE)
	public int getCurrentLane(final IAgent agent) {
		return (Integer) agent.getAttribute(CURRENT_LANE);
	}
	
	@getter(DISTANCE_TO_GOAL)
	public double getDistanceToGoal(final IAgent agent) {
		return (Double) agent.getAttribute(DISTANCE_TO_GOAL);
	}
	
	@setter(DISTANCE_TO_GOAL)
	public void setDistanceToGoal(final IAgent agent, final double dg) {
		agent.setAttribute(DISTANCE_TO_GOAL, dg);
	}

	
	@action(name = "advanced_follow_driving", args = {
			@arg(name = "path", type = IType.PATH, optional = true, doc = @doc("a path to be followed.")),
			@arg(name = "target", type = IType.POINT, optional = true, doc = @doc("the target to reach")),
			@arg(name = IKeyword.SPEED, type = IType.FLOAT, optional = true, doc = @doc("the speed to use for this move (replaces the current value of speed)")),
			@arg(name = "time", type = IType.FLOAT, optional = true, doc = @doc("time to travel"))}, 
			doc = @doc(value = "moves the agent towards along the path passed in the arguments while considering the other agents in the network (only for graph topology)", returns = "the remaining time", examples = { "do osm_follow path: the_path on: road_network;" }))
		public Double primAdvancedFollow(final IScope scope) throws GamaRuntimeException {
			final IAgent agent = getCurrentAgent(scope);
			final double security_distance = getSecurityDistance(agent);
			final Double s = scope.hasArg(IKeyword.SPEED) ? scope.getFloatArg(IKeyword.SPEED) : getSpeed(agent);
			final Double t = scope.hasArg("time") ? scope.getFloatArg("time") : 1.0;
			
			final double maxDist = computeDistance(scope, agent,s,t);
			final int currentLane = getCurrentLane(agent);
			final Double probaChangeLaneUp = getProbaLaneChangeUp(agent);
			final Double probaChangeLaneDown = getProbaLaneChangeDown(agent);
			final Boolean rightSide = getRightSideDriving(agent);
			final IAgent currentRoad = (IAgent) getCurrentRoad(agent);
			final ITopology topo = computeTopology(scope, agent);
			if ( topo == null ) {
				return 0.0;
			}
			final GamaPoint target = scope.hasArg("target") ? (GamaPoint) scope.getArg("target", IType.NONE) : null;
			final GamaPath path = scope.hasArg("path") ? (GamaPath) scope.getArg("path", IType.NONE) : null;
			if ( path != null && !path.getEdgeList().isEmpty() ) {
				double tps = t * moveToNextLocAlongPathOSM(scope, agent, path, target, maxDist, security_distance, currentLane, currentRoad,probaChangeLaneUp,probaChangeLaneDown,rightSide);	
				if (tps < 1.0)
					agent.setAttribute(REAL_SPEED, this.getRealSpeed(agent) / ((1 - tps)));
				
				return tps;
			}
			return 0.0;
		}
		


		/**
		 * @throws GamaRuntimeException
		 *             Return the next location toward a target on a line
		 * 
		 * @param coords coordinates of the line
		 * @param source current location
		 * @param target location to reach
		 * @param distance max displacement distance
		 * @return the next location
		 */

		protected double computeDistance(final IScope scope, final IAgent agent, final double s,final double t) throws GamaRuntimeException {
			
			return s * t * scope.getClock().getStep();
		}

		private double avoidCollision(final IScope scope, final IAgent agent, final double distance,
				final double security_distance, final GamaPoint currentLocation, final GamaPoint target,
				final int lane, final IAgent currentRoad, boolean changeLane) {
				IList agents = (IList) ((GamaList) currentRoad.getAttribute("agents_on")).get(lane);
				double vL = getVehiculeLength(agent);
				if (agents.size() < 2) {
					if (changeLane && distance < vL)
						return 0;
					return distance;
				}
				double distanceMax = distance + security_distance +  vL;
				
				List<IAgent> agsFiltered = new GamaList(agent.getTopology().getNeighboursOf(scope,agent.getLocation(), distanceMax, In.list(scope, agents)));
				
				if (agsFiltered.isEmpty())
					return distance;
				
				double distanceToGoal = agent.euclidianDistanceTo(target);//getDistanceToGoal(agent);
				//double distanceMax = distance + security_distance +  0.5 * getVehiculeLength(agent);
				IAgent nextAgent = null;
				double minDiff = Double.MAX_VALUE;
				for (IAgent ag : agsFiltered) {
					double dist = ag.euclidianDistanceTo(target);
					double diff = (distanceToGoal - dist) ;
					if (changeLane && diff < vL) return 0;
					if (diff <  minDiff && ((changeLane && diff >= 0) || (! changeLane && diff > 0)) )  {
						minDiff = diff;
						nextAgent = ag;
					}
				}
				
				if (nextAgent == null)
					return distance;
				
				double realDist = Math.min(distance, minDiff - security_distance - 0.5 * vL - 0.5 * getVehiculeLength(nextAgent) );
				if (changeLane && realDist < vL) 
					return 0;
				return Math.max(0.0,realDist );
			}
		
		private void changeLane(final IScope scope, final IAgent agent, int previousLane, int newLane,final IAgent currentRoad,List agentOn ){
			agent.setAttribute(CURRENT_LANE, newLane);
			((List)(agentOn.get(previousLane))).remove(agent);
			((List)(agentOn.get(newLane))).add(agent);
		}
		private double avoidCollision(final IScope scope, final IAgent agent, final double distance,
				final double security_distance, final GamaPoint currentLocation, final GamaPoint target,
				final int lane, final IAgent currentRoad,final Double probaChangeLaneUp,final Double probaChangeLaneDown, final Boolean rightSide) {
				double distMax = 0;
				int newLane = lane;
				List agentOn = (List) currentRoad.getAttribute("agents_on");
				if (lane > 0 && GAMA.getRandom().next() < probaChangeLaneDown) {
					double val = avoidCollision(scope,agent,distance,security_distance, currentLocation,target,lane - 1, currentRoad, true);
					//System.out.println(agent + " lane - 1 : " + val);
					if (val == distance) {
						newLane = lane - 1;
						changeLane(scope, agent, lane, newLane,currentRoad,agentOn );
						return distance;
					}
					if (val > distMax && val > 0) {
						newLane = lane - 1;
						distMax = val;
					}
				}
				double val = avoidCollision(scope,agent,distance,security_distance, currentLocation,target,lane, currentRoad, false);
			//	System.out.println(agent + " lane : " + val);
				if (val == distance) {
					//changeLane(scope, agent, lane, newLane,currentRoad,agentOn );
					return distance;
				}
				if (val >= distMax) {
					distMax = val;
					newLane = lane;
				}
				if (lane <  ((Integer) currentRoad.getAttribute("lanes") - 1) && GAMA.getRandom().next() < probaChangeLaneUp) {
					val = avoidCollision(scope,agent,distance,security_distance, currentLocation,target,lane+1, currentRoad, true);
					//System.out.println(agent + " lane + 1 : " + val);
					if (val > distMax && val > 0) {
						distMax = val; 
						newLane = lane + 1;
					}
				}

				if (lane != newLane) changeLane(scope, agent, lane, newLane,currentRoad,agentOn );
				//System.out.println(agent + " newLane : " + newLane + " distMax: " + distMax);
				return distMax;
			}
		

		private GamaPoint computeRealTarget( final IAgent agent, 
			final double security_distance, final GamaPoint currentLocation, final GamaPoint target,
			final int lane, final IAgent currentRoad) {
			
		//	System.out.println("currentRoad : " + currentRoad);
		//	System.out.println("currentRoad.getAttribute(agents_on)) : " + currentRoad.getAttribute("agents_on"));
			
			List<IAgent> agents = (List<IAgent>) ((GamaList) currentRoad.getAttribute("agents_on")).get(lane);
			if (agents.size() < 2)
				return target;
			//System.out.println("agents : " + agents);
			double distanceToGoal = agent.euclidianDistanceTo(target);//getDistanceToGoal(agent);
			IAgent nextAgent = null;
			double minDiff = Double.MAX_VALUE;
			for (IAgent ag : agents) {
				if (ag == agent) continue;
				double dist = ag.euclidianDistanceTo(target);//getDistanceToGoal(ag);
				double diff = (distanceToGoal - dist) ;
				if (dist < distanceToGoal && diff <  minDiff ) {
					minDiff = diff;
					nextAgent = ag;
				}
			}
			if (nextAgent == null)
				return target;
			//System.out.println("currentRoad.getAttribute(agents_on)).get(index - 1) : " + ((GamaList) ((GamaList) currentRoad.getAttribute("agents_on")).get(lane)).get(index - 1));
			double buff_dist = getVehiculeLength(agent) + getVehiculeLength(nextAgent) + security_distance;
			IShape shape = Transformations.enlarged_by(nextAgent.getLocation(), buff_dist);
			IShape shapeInter = Operators.inter(currentRoad, shape);
			//System.out.println("ICI\nnextAgent.getLocation() : " + nextAgent.getLocation());
			//System.out.println("currentRoad : " + currentRoad.getGeometry());
			
			
			//return target; 
			if (shapeInter != null) {
				return (GamaPoint) Punctal._closest_point_to(currentLocation, shapeInter);
			}
			return target;
		}

		private double moveToNextLocAlongPathOSM(final IScope scope, final IAgent agent, final IPath path, final GamaPoint target, final double _distance, final double security_distance, int currentLane, final  IAgent currentRoad, final Double probaChangeLaneUp, final Double probaChangeLaneDown, final Boolean rightSide ) {
			
			GamaPoint currentLocation = (GamaPoint) agent.getLocation().copy(scope);
			GamaPoint falseTarget = target == null ? new GamaPoint(currentRoad.getInnerGeometry().getCoordinates()[currentRoad.getInnerGeometry().getCoordinates().length]) : target;
			/*for (Object ag : path.getEdgeList()) {
				System.out.println(ag + " -> " + ((IAgent) ag).getGeometry());
			}*/
		
			//final GamaPoint falseTarget = pt_target; //computeRealTarget(agent, security_distance, currentLocation, pt_target, currentLane, currentRoad);
			final GamaList indexVals =  initMoveAlongPath(agent, path, currentLocation, falseTarget, currentRoad);
			if (indexVals == null) return 0.0;
			int indexSegment = (Integer) indexVals.get(0);
			final int endIndexSegment = (Integer) indexVals.get(1);
			//System.out.println("currentRoad : " + currentRoad.getGeometry());
			//System.out.println("currentLocation : " + currentLocation + " falseTarget : " + falseTarget);
			//System.out.println("indexSegment : " + indexSegment + " endIndexSegment : " + endIndexSegment);
			
			if (indexSegment > endIndexSegment) {
				return 0.0;
			}
			double distance = _distance;
			final GamaGraph graph = (GamaGraph) path.getGraph();
			double realDistance = 0;
			final IShape line = currentRoad.getGeometry();
			final Coordinate coords[] = line.getInnerGeometry().getCoordinates(); 
			
			
			for ( int j = indexSegment; j <= endIndexSegment; j++ ) {
				GamaPoint pt = null;
				if ( j == endIndexSegment ) {
					pt = falseTarget;
				} else {
					pt = new GamaPoint(coords[j]);
				}
			//	System.out.println("j : " + j + " endIndexSegment : " + endIndexSegment + " pt : " + pt);
				
				double dist = pt.euclidianDistanceTo(currentLocation);
				
				distance =
						avoidCollision(scope, agent, distance, security_distance, currentLocation, falseTarget, currentLane, currentRoad,probaChangeLaneUp,probaChangeLaneDown ,rightSide);
				currentLane = (Integer) agent.getAttribute(CURRENT_LANE);
				if ( distance < dist ) {
					final double ratio = distance / dist;
					final double newX = currentLocation.getX() + ratio * (pt.getX() - currentLocation.getX());
					final double newY = currentLocation.getY() + ratio * (pt.getY() - currentLocation.getY());
					GamaPoint npt = new GamaPoint(newX,newY);
					realDistance += currentLocation.euclidianDistanceTo(npt);
					currentLocation.setLocation(npt);
					distance = 0;
					break;
				} else {
					currentLocation = pt;
					distance = distance - dist;
					realDistance += dist; 
					if (j == endIndexSegment ) {
						break;
					}
					indexSegment++;
				}
			}
			//path.setIndexSegementOf(agent, indexSegment);
			agent.setLocation(currentLocation);
			path.setSource(currentLocation.copy(scope));
			agent.setAttribute(REAL_SPEED, realDistance / scope.getClock().getStep());
			setDistanceToGoal(agent, currentLocation.euclidianDistanceTo(falseTarget));
			//System.out.println("_distance : " + _distance);
			//System.out.println("distance : " + distance);
			return _distance == 0.0 ? 1.0 : (distance / _distance) ;
		}
		
		protected GamaList initMoveAlongPath(final IAgent agent, final IPath path, final GamaPoint currentLocation,final GamaPoint falseTarget, final IAgent currentRoad) {
			final GamaList initVals = new GamaList();
			Integer indexSegment = 0;
			Integer endIndexSegment = 0;
			final IList<IShape> edges = path.getEdgeGeometry();
			if (edges.isEmpty()) return null;
			//final int nb = edges.size();
			if ( currentRoad.getInnerGeometry().getNumPoints() == 2 ) {
				indexSegment = 0;
				endIndexSegment = 0;
				
			} else {
				double distanceS = Double.MAX_VALUE;
				double distanceT = Double.MAX_VALUE;
				IShape line = currentRoad.getGeometry();
				final Point pointS = (Point) currentLocation.getInnerGeometry();
				final Point pointT = (Point) falseTarget.getInnerGeometry();
				final Coordinate coords[] = line.getInnerGeometry().getCoordinates();
				final int nbSp = coords.length;
				final Coordinate[] temp = new Coordinate[2];
				for ( int i = 0; i < nbSp - 1; i++ ) {
					temp[0] = coords[i];
					temp[1] = coords[i + 1];
					final LineString segment = GeometryUtils.factory.createLineString(temp);
					final double distS = segment.distance(pointS);
					if ( distS < distanceS ) {
						distanceS = distS;
						indexSegment = i + 1;
					}
					final double distT = segment.distance(pointT);
					if ( distT < distanceT ) {
						distanceT = distT;
						endIndexSegment = i + 1;
					}
				}
			}
			initVals.add(indexSegment);
			initVals.add(endIndexSegment);
			return initVals;
		}
}