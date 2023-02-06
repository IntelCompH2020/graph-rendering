import { NgModule } from '@angular/core';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { TrainingModelProgressComponent } from './training-model-progress.component';

@NgModule({
	imports: [CommonUiModule],
	declarations: [TrainingModelProgressComponent],
	exports: [TrainingModelProgressComponent],
	entryComponents: [TrainingModelProgressComponent]
})
export class TrainingProgressModelDialogModule {
	constructor() { }
}
