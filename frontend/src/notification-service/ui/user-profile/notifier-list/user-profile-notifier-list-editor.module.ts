import { NgModule } from '@angular/core';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { UserProfileNotifierListEditorComponent } from '@notification-service/ui/user-profile/notifier-list/user-profile-notifier-list-editor.component';

@NgModule({
	imports: [
		CommonUiModule,
		CommonFormsModule,
	],
	declarations: [
		UserProfileNotifierListEditorComponent
	],
	exports: [
		UserProfileNotifierListEditorComponent
	]
})
export class UserProfileNotifierListModule { }
