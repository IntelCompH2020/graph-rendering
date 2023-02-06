import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GraphComponent } from "./graphs.component";
import { WordListsRoutingModule } from "./graphs-routing.module";
import { ListingModule } from '@common/modules/listing/listing.module';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { CommonFormsModule } from '@common/forms/common-forms.module';
import { GraphService } from '@app/core/services/http/graph.service';
import { GraphPickerDialogComponent } from './graph-picker-dialog/graph-picker-dialog.component';

@NgModule({
  declarations: [
    GraphComponent,
    GraphPickerDialogComponent
  ],
  imports: [
    CommonModule,
    ListingModule,
    CommonUiModule,
    WordListsRoutingModule,
    CommonFormsModule
  ],
  providers:[
    GraphService,
  ]
})
export class WordListsModule { }
