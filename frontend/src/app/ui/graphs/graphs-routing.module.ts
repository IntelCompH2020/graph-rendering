import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { GraphComponent } from "./graphs.component";

const routes: Routes = [
	{
		path: '',
		component: GraphComponent,
		children:[
			{ path: '', component: GraphComponent },
		]
	},
	{ path: '**', loadChildren: () => import('@common/page-not-found/page-not-found.module').then(m => m.PageNotFoundModule) },
];

@NgModule({
	imports: [RouterModule.forChild(routes)],
	exports: [RouterModule]
})
export class WordListsRoutingModule { }
