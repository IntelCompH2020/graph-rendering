import {Component, Inject} from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { GraphInfoLookup } from '@app/core/model/graph/graph-info.lookup';
import { Guid } from '@common/types/guid';



@Component({
    templateUrl:'./graph-picker-dialog.component.html',
    styleUrls:[
        './graph-picker-dialog.component.scss'
    ]
})
export class GraphPickerDialogComponent{


    selectedItem:GraphInfoItem = null;

    availableGraphs: GraphInfoItem[];




    constructor(
        private dialogRef: MatDialogRef<GraphPickerDialogComponent>,
        @Inject(MAT_DIALOG_DATA) data: GraphPickerDialogData
    ){

        if(!data?.items){
            console.warn("no data was found in graph picker dialog");
            this.cancel();
        }
        this.availableGraphs = data.items;
    }



    submit():void{
        this.dialogRef.close(this.selectedItem?.graphInfo);
    }

    cancel(): void{
        this.dialogRef.close();      
    }
}




export interface GraphPickerDialogData {
    items:GraphInfoItem[];
}


export interface GraphPickerDialogResponse{
    nodeIds: Guid[];
    edgeIds: Guid[];
}

interface GraphInfoItem{
    name: string;
    graphInfo: GraphPickerDialogResponse;
}
