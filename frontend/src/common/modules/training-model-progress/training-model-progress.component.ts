import { Component, OnInit } from '@angular/core';
import { MatDialogRef} from '@angular/material/dialog';

@Component({
  selector: 'app-training-model-progress',
  templateUrl: './training-model-progress.component.html',
  styleUrls: ['./training-model-progress.component.scss']
})
export class TrainingModelProgressComponent implements OnInit {

  runningTime = '00:10:45';
  epochTime = '00:10:45';
  progressPercent = '75';
  progressInfo = 'Loading...';


  constructor(
   private dialogRef: MatDialogRef<TrainingModelProgressComponent>
  ) { }

  ngOnInit(): void {
  }


  finish(): void{
    this.dialogRef.close(false);
  }
  hide(): void{
    this.dialogRef.close(true);
  }

  details = [
    {
      label: 'Training model',
      value: 'nlp-cordis-80-topics'
    },
    {
      label: 'Type',
      value: 'LDA /MALLET'
    },
    {
      label: 'Topics',
      value: '50'
    },
    {
      label: 'Corpus',
      value: 'CORDIS'
    },
    {
      label: 'Train size',
      value: '20308'
    },
    {
      label: 'Validation size',
      value: '9129'
    },
    {
      label: 'Batch size',
      value: '8'
    },
  ]


  summaries = [
    {
      label: 'Epoch',
      value: '1'
    },
    {
      label: 'Step',
      value: '78'
    },
    {
      label: 'Train Loss',
      value: '0.2616',
    },
    {
      label: 'Validation Loss',
      value: '0.4654'
    },
    {
      label: 'Best epoch',
      value: ''
    }
  ]
}