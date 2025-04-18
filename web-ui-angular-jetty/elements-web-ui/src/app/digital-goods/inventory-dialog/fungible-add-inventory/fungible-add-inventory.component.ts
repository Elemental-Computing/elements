import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Item } from 'src/app/api/models';
import { FungibleInventoryService } from 'src/app/api/services/fungible-inventory.service';
import { ItemSelectDialogComponent } from '../item-select-dialog/item-select-dialog.component';
import {ItemCategory} from "../../../api/models/item";

@Component({
  selector: 'fungible-app-add-inventory',
  templateUrl: './fungible-add-inventory.component.html',
  styleUrls: ['./fungible-add-inventory.component.css']
})
export class FungibleAddInventoryComponent implements OnInit {

  @Input()
  userId: string;

  @Output("refresh")
  refresh: EventEmitter<any> = new EventEmitter();

  selectedItem: Item;

  constructor(
    private inventoryService: FungibleInventoryService,
    public dialog: MatDialog,
    private snackBar: MatSnackBar
  ) { }

  ngOnInit() {
  }

  createInventory(name: string, priority: number, quantity: number){
    this.inventoryService.createInventoryItemAdvanced({
      userId: this.userId,
      itemId: name,
      quantity,
      priority
    }).subscribe(
      data => {
        this.refresh.emit();
        this.snackBar.open("Item Created", 'Dismiss', { duration: 3000, panelClass: ['green-snackbar'] });
      },
      err => {
        if(err.code === "DUPLICATE"){
          this.snackBar.open(`⚠ ERROR: Item already exists. Please select a unique Stack Priority.`, 'Dismiss', { duration: 3000 });
        } else {
          this.snackBar.open(`${err.code}`, 'Dismiss', { duration: 3000 });
        }
      }
    )
  }

  showFindItemDialog() {
    this.dialog.open(ItemSelectDialogComponent, {
      width: '500px',
      data: {
        category: ItemCategory.FUNGIBLE,
        next: result => {
          this.selectedItem = result;
        }
      }
    });
  }

}
