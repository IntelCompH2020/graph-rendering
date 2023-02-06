import { NgModule } from '@angular/core';
import { DataTableDateFormatPipe, DateFormatPipe } from '@common/formatting/pipes/date-format.pipe';
import { DataTableDateTimeFormatPipe, DateTimeFormatPipe } from '@common/formatting/pipes/date-time-format.pipe';
import { DataTableDomainModelTypeFormatPipe } from './pipes/domain-model-type.pipe';
import { DataTableTopicModelTypeFormatPipe } from './pipes/topic-model-type.pipe';

//
//
// This is shared module that provides all formatting utils. Its imported only once on the AppModule.
//
//
@NgModule({
	declarations: [
		DateFormatPipe,
		DateTimeFormatPipe,
		DataTableDateFormatPipe,
		DataTableDateTimeFormatPipe,
		DataTableTopicModelTypeFormatPipe,
		DataTableDomainModelTypeFormatPipe
	],
	exports: [
		DateFormatPipe,
		DateTimeFormatPipe,
		DataTableDateFormatPipe,
		DataTableDateTimeFormatPipe,
		DataTableTopicModelTypeFormatPipe,
		DataTableDomainModelTypeFormatPipe
	],
	providers: [
		DateFormatPipe,
		DateTimeFormatPipe,
		DataTableDateFormatPipe,
		DataTableDateTimeFormatPipe,
		DataTableTopicModelTypeFormatPipe,
		DataTableDomainModelTypeFormatPipe
	]
})
export class CommonFormattingModule { }
