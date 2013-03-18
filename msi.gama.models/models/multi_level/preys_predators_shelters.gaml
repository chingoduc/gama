model preys_predators_shelters

global { 
	rgb prey_color <- rgb ('green') const: true;
	float prey_perception <- 3.0;
	float prey_size <- 2.0 const: true;
	float prey_speed <- 1.0;
	rgb prey_flee_color <- rgb ('orange');
	float prey_invisible_speed <- 3 * prey_speed;
	rgb prey_invisible_color <- rgb ('black');  
	int prey_in_shelter_max_time min: 1 init: 200;
	int prey_invisible_max_time min: 1 max: 100 init: 70; 
	int number_of_prey min: 1 max: 1000 init: 100;
	
	rgb predator_color <- rgb ('red') const: true;
	float predator_perception <- 3.0;
	float predator_size <- 4.0;
	float predator_speed <- 1.0;
	int number_of_predator min: 1 max: 100 init: 30; 
	
	rgb predator_in_shelter_color <- rgb ('yellow') const: true;
	 
	rgb shelter_color <- rgb ('blue') const: true; 
	float shelter_speed <- 1.5 const: true;
	geometry shelter_shape <- square (50.0);
	int number_of_shelter <- 2 const: true;
	
	
	init {
		create prey number: number_of_prey;
		create predator number: number_of_predator; 
		create shelter number: number_of_shelter returns: shelters;
		set (shelters at 0).shape value: shelter_shape at_location {150, 250};
		set (shelters at 1).shape value: shelter_shape at_location {350, 250};
	}
}

entities {  
	species prey skills: [moving] control: fsm {
		geometry shape <- circle (prey_size);
		rgb color <- prey_color;
		list nearby_predators of: predator value: (agents_overlapping (shape + prey_perception)) of_species predator;
		int invisible_time min: 1 init: int(time);

		shelter nearest_shelter;		

		state move_around initial: true {
			enter {
				set speed value: prey_speed;
				set color value: prey_color;
			}
			do wander; 
			transition to: flee_predator when: !(empty (nearby_predators)); 
		}
		
		
		state flee_predator {
			enter {
				set color value: prey_flee_color;
				set nearest_shelter value: first ( (list (shelter)) sort_by ( each distance_to (self)) );
			}
			if !(empty (nearby_predators)) { do move heading: (self) towards (nearest_shelter) speed: prey_speed;}
			transition to: move_around when: (empty (nearby_predators));
		}
		
		state invisible {
			enter {
				set speed value: prey_invisible_speed;
				set color value: prey_invisible_color;
				set invisible_time value: time;
				set heading value: rnd (359) ;
			}
			do move;
			transition to: move_around when: ( (time - invisible_time) > prey_invisible_max_time );
		}
		
		aspect default {
			draw shape color: color;
		}
	}
	
	species predator skills: [moving] schedules: shuffle (list (predator)) {
		geometry shape <- circle (predator_size);
		prey target_prey value: self choose_target_prey [];
		
		action choose_target_prey type: prey {
			if ( (target_prey = nil) or (dead (target_prey) ) ) {
				return one_of ( (list (prey)) where (each.state = 'move_around') );
			}
			
			return target_prey;
		}
		
		reflex move_around when: (target_prey = nil) { do wander speed: predator_speed; }
		
		reflex chase_prey when: (target_prey != nil) { do move heading: self towards target_prey speed: predator_speed;}
		
		aspect default {
			draw shape color: predator_color;
		} 
	} 
	
	species shelter skills: [moving]  frequency: 2 {
		geometry shape <- (circle (50.0)) at_location {250, 250};
		list chased_preys of: prey value: (list (prey)) where ( (each.shape intersects shape) and (each.state = 'flee_predator') );
		
		reflex move_around {
			do wander speed: shelter_speed; 
		}
		 
		reflex capture_chased_preys when: !(empty (chased_preys)) { 
			capture chased_preys as: prey_in_shelter {
				set state value: 'in_shelter'; 
				set shape value: ( triangle (4.0) ) at_location location;
			}
		}
		
		reflex release_member_preys {
			let to_be_released type: list of: prey_in_shelter value: (list (prey_in_shelter)) where ( (time - each.in_shelter_time) > prey_in_shelter_max_time );
			 
			release to_be_released in: world as: prey { 
				set state value: 'invisible';
				set shape value:  at_location (circle (prey_size), self.location);   
			}
		} 
		
		
		species prey_in_shelter parent: prey frequency: 2 schedules: ( ( int ( (length (prey_in_shelter)) / 2 ) ) among (list (prey_in_shelter)) ) {
			var in_shelter_time type: int init: int(time);
			
			state in_shelter {
				do wander speed: shelter_speed;
			}
			
			aspect default {
				draw shape color: predator_in_shelter_color;
			}
		}
		
		aspect default {
			draw shape color: shelter_color;
			draw text: 'Members: ' + (string (length ((members)))) color: rgb ('white') size: 8 at: {(location).x - 20, (location).y};
		}
	}

}

environment width: 500 height: 500;

experiment default_experiment type: gui {
	output {
		display default_display {
			species prey;
			species predator transparency: 0.5;
			species shelter transparency: 0.5 {
				species prey_in_shelter;
			}
		}
	}
}