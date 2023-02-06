import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ModelSelectionService } from '@app/core/services/ui/model-selection.service';
import { TrainingQueueItem, TrainingQueueService } from '@app/core/services/ui/training-queue.service';
import { TrainingModelProgressComponent } from '@common/modules/training-model-progress/training-model-progress.component';

@Component({
  selector: 'app-footer',
  templateUrl: './footer.component.html',
  styleUrls: ['./footer.component.scss']
})
export class FooterComponent implements OnInit {

  trainingModels: Readonly<TrainingQueueItem[]>;

  constructor( 
    private trainingModelQueueService: TrainingQueueService,
    protected modelSelecrionService: ModelSelectionService,
    private dialog: MatDialog
  ) {
    this.trainingModels = this.trainingModelQueueService.queue;
  }

  ngOnInit(): void {}


  makeAllFinished(): void{
    this.trainingModelQueueService.makeAllfinished();
  }

  openTrainingDialog(): void{
    this.dialog.open(TrainingModelProgressComponent, {
      minWidth: '80vw',
      disableClose: true,
    })
  }

}
