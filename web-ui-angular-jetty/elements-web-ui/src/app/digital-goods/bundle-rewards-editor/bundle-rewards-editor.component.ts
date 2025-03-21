import {Component, ElementRef, Input, OnInit, ViewChild} from '@angular/core';
import {Reward} from '../../api/models/reward';
import {FormBuilder, FormControl, Validators} from '@angular/forms';
import {ItemsService} from '../../api/services/items.service';
import {ItemExistsValidator} from '../item-exists-validator';
import {Item} from '../../api/models/item';
import {BundleReward} from '../../api/models/bundle-reward';

@Component({
  selector: 'app-bundle-rewards-editor',
  templateUrl: './bundle-rewards-editor.component.html',
  styleUrls: ['./bundle-rewards-editor.component.css']
})
export class BundleRewardsEditorComponent implements OnInit {
  @Input() rawRewards: Array<BundleReward>;
  @ViewChild('newRewardItem') newItemField: ElementRef;

  constructor(private formBuilder: FormBuilder, private itemsService: ItemsService) {}

  private itemExistsValidator = new ItemExistsValidator(this.itemsService);

  public newRewardForm = this.formBuilder.group({
    newRewardItem: ['', [], [this.itemExistsValidator.validate]],
    newRewardCt: ['', [Validators.required, Validators.pattern('^[0-9]+$')]]
  });

  public rewards: Array<Reward> = [];

  public existingRewardForm = this.formBuilder.group({});

  public addReward(itemName: string, itemCt: number, isUserSourced = true) {
    // block request if form not valid
    if (!this.newRewardForm.valid && isUserSourced) { return; }

    // get item specified by form
    this.itemsService.getItemByIdentifier(itemName).subscribe((item: Item) => {
      // add to rewards item-array
      this.rewards.push({
        item: item,
        quantity: itemCt
      });

      // add formControl
      this.existingRewardForm.addControl('reward' + (this.rewards.length - 1) + 'Item',
        new FormControl(this.rewards[this.rewards.length - 1].item.name, [Validators.required], [this.itemExistsValidator.validate]));
      this.existingRewardForm.addControl('reward' + (this.rewards.length - 1) + 'Ct',
        new FormControl(this.rewards[this.rewards.length - 1].quantity, [Validators.required, Validators.pattern('^[0-9]+$')]));

      if (isUserSourced) {
        // focus or blur on new name field?
        this.newItemField.nativeElement.focus();

        // clear form fields
        this.newRewardForm.reset();
      }
    });
  }

  public getRawRewards() {
    const rawRewards: Array<BundleReward> = [];
    for (let i = 0; i < this.rewards.length; i++) {
      rawRewards.push({
        itemId: this.rewards[i].item.id,
        quantity: this.rewards[i].quantity
      });
    }

    return rawRewards;
  }

  removeReward(index: number) {
    if(this.rewards.length > 1) {
      this.rewards.splice(index, 1);
    } else {
      alert("At least one reward is required for each step. Add another reward before deleting this one.");
    }
  }

  updateReward(reward: Reward, param: string, event: any) {
    reward[param] = event.target.value;
  }

  ngOnInit() {
    this.rawRewards = this.rawRewards || [];
    for (let i = 0; i < this.rawRewards.length; i++) {
      this.addReward(this.rawRewards[i].itemId, this.rawRewards[i].quantity, false);
    }
  }

}
