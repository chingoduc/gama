/**
 * Purpose: Test that direct micro/macro-species can have the same parent-species if that parent-species doesn't have micro-species.
 * 
 * Action(s):
 * 		1. Load the model.
 * 
 * Expected outcome: 
 * 		1. The model can be loaded successfully.
 */
model testcase40

import "platform:/plugin/msi.gama.application/generated/std.gaml"

global {
}

entities {
	species A skills: situated parent: C {
		species B parent: C {
			
		}
	}
	
	species C {
		
	}

}

environment width: 100 height: 100 {
}

experiment default_expr type: gui {
	
}


