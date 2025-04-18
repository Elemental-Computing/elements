import {Component, Inject, OnInit, ViewChild} from '@angular/core';
import {FormBuilder, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {MatChipInputEvent} from '@angular/material/chips';
import {MatSnackBar} from '@angular/material/snack-bar'
import {COMMA, ENTER} from '@angular/cdk/keycodes';
import {JsonEditorCardComponent} from '../../json-editor-card/json-editor-card.component';
import {MissionStepsCardComponent} from './mission-steps-card/mission-steps-card.component';
import {AlertService} from '../../alert.service';

@Component({
  selector: 'app-item-dialog',
  templateUrl: './mission-dialog.component.html',
  styleUrls: ['./mission-dialog.component.css']
})
export class MissionDialogComponent implements OnInit {

  constructor(public dialogRef: MatDialogRef<MissionDialogComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private formBuilder: FormBuilder, private alertService: AlertService, private snackBar: MatSnackBar) { }

  @ViewChild(JsonEditorCardComponent) editorCard: JsonEditorCardComponent;
  @ViewChild(MissionStepsCardComponent) stepsCard: MissionStepsCardComponent;

  originalMetadata = JSON.parse(JSON.stringify(this.data.mission.metadata || {}));
  originalSteps = JSON.parse(JSON.stringify(this.data.mission.steps || []));
  originalFinalStep = JSON.parse(JSON.stringify(this.data.mission.finalRepeatStep || null));

  selectable = true;
  removable = true;
  addOnBlur = true;
  readonly separatorKeyCodes: number[] = [ENTER, COMMA];

  missionForm = this.formBuilder.group({
    name: [ this.data.mission.name, [Validators.required, Validators.pattern('^[_a-zA-Z0-9]+$') ]],
    displayName: [ this.data.mission.displayName, [Validators.required]],
    description: [ this.data.mission.description, [Validators.required]],
    tags: []
  });

  addTag(event: MatChipInputEvent): void {
    const input = event.input;
    const value = event.value;

    if ((value || '').trim()) {
      if (!this.data.mission.tags) { this.data.mission.tags = []; }
      this.data.mission.tags.push(value);
    }

    if (input) {
      input.value = '';
    }
  }

  remove(tag: string): void {
    const index = this.data.mission.tags.indexOf(tag);

    if (index >= 0) {
      this.data.mission.tags.splice(index, 1);
    }
  }

  /*
  * Can't just call dialogRef.close(missionForm.value) since it doesn't accurately
  * capture changes to item tags so we need to explicitly attach the entire tag
  * array to the itemForm, overwriting the initial tags value
  */
  close(saveChanges?: boolean): void {
    // simply close editor without making any changes to data
    if (!saveChanges) {
      this.dialogRef.close();
      this.data.mission.metadata = this.originalMetadata;
      this.data.mission.steps = this.originalSteps;
      this.data.mission.finalRepeatStep = this.originalFinalStep;
      return;
    }

    const formData = this.missionForm.value;
    if (this.data.mission.tags !== undefined) {
      formData.tags = this.data.mission.tags;
    }

    this.editorCard.validateMetadata(true);
    if (this.data.mission.metadata !== undefined) {
      formData.metadata = this.data.mission.metadata;
    }

    // TODO get steps and final repeat ste pfrom mission card
    // mission card has reference to this.data.mission, can attach steps to it if told to
    if (this.data.mission.steps) {
      formData.steps = this.data.mission.steps;
      for(let i = 0; i < formData.steps.length; i++) {
        delete formData.steps[i].isNew;
      }
    }
    if (this.data.mission.finalRepeatStep) {
      formData.finalRepeatStep = this.data.mission.finalRepeatStep;
      delete formData.finalRepeatStep.isNew;
    }

    if (!this.stepsCard.stepsValid()) {
      return;
    }

    this.data.next(formData).subscribe(() => {
      this.dialogRef.close();
      this.data.refresher.refresh();
      this.data.mission.steps = [];
    }, err => {
      this.alertService.error(err);
    });
  }

  ngOnInit() {
    this.alertService.getMessage().subscribe((message: any) => {
      if(message) {
        this.snackBar.open(message.text, "Dismiss", { duration: 3000 });
      }
    });
  }

  missionValid() {
    return this.missionForm.valid &&
      (this.data.mission.finalRepeatStep ||
        (this.data.mission.steps && this.data.mission.steps.length > 0 &&
          (this.stepsCard && this.stepsCard.stepsValid())));
  }

}
