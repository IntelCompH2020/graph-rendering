import { NgModule } from '@angular/core';
import { HomeRoutingModule } from '@app/ui/home/home-routing.module';
import { HomeComponent } from '@app/ui/home/home.component';
import { CommonUiModule } from '@common/ui/common-ui.module';
import { OverviewCardComponent } from './overview-card/overview-card.component';
import { GraphListComponent } from './graph-list-item/graph-list-item-card.component';
@NgModule({
	imports: [
		CommonUiModule,
		HomeRoutingModule
	],
	declarations: [
		HomeComponent,
  OverviewCardComponent,
  GraphListComponent,
	]
})
export class HomeModule { }
