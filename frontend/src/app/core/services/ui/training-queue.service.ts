import { Injectable } from '@angular/core';

@Injectable()
export class TrainingQueueService {

    private _queue: TrainingQueueItem[] = [];


    get queue(): Readonly<TrainingQueueItem[]>{
        return this._queue;
    }

	constructor() {
		
	}


    public addItem(item: TrainingQueueItem): void{
        this._queue.push(item);
    }


    public makeAllfinished(): void{
        this._queue.forEach(  item => item.finished = true)
    }

}



export interface TrainingQueueItem{
    label: string;
    finished?: boolean;
    id?: string;
}