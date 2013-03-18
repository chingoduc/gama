model voters

global {  
	int width  <- 200 min: 10 max: 400;
	int height <- 200 min: 10 max: 400;  
	bool torus <- true;
	int density  <- 80 min: 1 max: 99; 
	rgb livingcolor <- rgb('white');
	rgb deadcolor <- rgb('black');
	bool random_choice <- true ; 
}

environment width: width height: height {
	grid life_grid width: width height: height neighbours: 8 torus: torus {
		int living <- (self neighbours_at 1) count (each.state) update: (self neighbours_at 1) count (each.state);
		bool state <- (rnd(100)) < density update: (living = 4) ? (random_choice ? flip(0.5) : state): ((living > 4) ? true : false) ;
		rgb color <-  state ? livingcolor : deadcolor update: state ? livingcolor : deadcolor ;
	}
}

	
experiment voters type: gui{
	
	parameter 'Width:' var: width  category: 'Board' ;
	parameter 'Height:' var: height category: 'Board' ;  
	parameter 'Torus?:' var: torus category: 'Board' ;
	parameter 'Initial density of live cells:' var: density category: 'Cells' ; 
	parameter 'Color of live cells:' var: livingcolor category: 'Colors' ;
	parameter 'Color of dead cells:' var: deadcolor category: 'Colors' ;
	parameter 'Random choice in case of tie ?' var: random_choice category: 'Cells' ; 
	
	output {
		display Life {
			grid life_grid ;
		}
	}
}

