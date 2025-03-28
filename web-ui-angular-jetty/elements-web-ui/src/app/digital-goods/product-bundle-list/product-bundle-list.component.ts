import {AfterViewInit, Component, Input, OnInit, ViewChild} from '@angular/core';
import {ApplicationConfigurationsDataSource} from '../../applications/application-configuration.datasource';
import {ProductBundle} from '../../api/models/product-bundle';
import {filter} from 'rxjs/operators';
import {ConfirmationDialogService} from '../../confirmation-dialog/confirmation-dialog.service';
import {MatDialog} from '@angular/material/dialog';
import {MatPaginator} from '@angular/material/paginator';
import {MatTableDataSource} from '@angular/material/table'
import {ProductBundleEditorComponent} from '../product-bundle-editor/product-bundle-editor.component';
import {ProductBundleViewModel} from '../../models/product-bundle-view-model';
import {SelectionModel} from '@angular/cdk/collections';

@Component({
  selector: 'app-product-bundle-list',
  templateUrl: './product-bundle-list.component.html',
  styleUrls: ['./product-bundle-list.component.css']
})
export class ProductBundleListComponent implements OnInit, AfterViewInit {
  @Input() productBundles: Array<ProductBundle>;

  @ViewChild(MatPaginator) paginator: MatPaginator;

  tableDataSource: MatTableDataSource<ProductBundle>;
  displayedColumns = ["select", "id", "displayName", "display", "actions"];
  selection: SelectionModel<ProductBundle>;
  hasSelection = false;

  constructor(private dialogService: ConfirmationDialogService,
              public dialog: MatDialog) {
    this.tableDataSource = new MatTableDataSource();
  }

  ngOnInit() {
    this.selection = new SelectionModel<ProductBundle>(true, []);
    if (!this.productBundles) { this.productBundles = new Array<ProductBundle>(); }
    this.refreshDataSource();
  }

  ngAfterViewInit() {
    this.paginator.pageSize = 10;
    this.paginator.length = this.productBundles.length;
    this.tableDataSource.paginator = this.paginator;
  }

  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.productBundles.length;
    return numRows === numSelected;
  }

  masterToggle() {
    this.isAllSelected() ?
      this.selection.clear() :
      this.productBundles.forEach(row => this.selection.select(row));
  }

  editProductBundle(productBundle: ProductBundle) {
    this.showDialog(false, ProductBundleEditorComponent, productBundle, res => {
      for (const prop in res) {
        if (res.hasOwnProperty(prop)) {
          productBundle[prop] = res[prop];
        }
      }
    });
  }

  deleteSelectedProductBundles() {
    this.dialogService
      .confirm('Confirm Dialog', `Are you sure you want to delete the ${this.selection.selected.length} selected product bundle${this.selection.selected.length == 1 ? '' : 's'}?`)
      .pipe(filter(r => r))
      .subscribe(res => {
        this.selection.selected.forEach(row => this.doDeleteProductBundle(row));
        this.selection.clear();
        this.refreshDataSource();
      });
  }

  deleteProductBundle(productBundle: ProductBundle) {
    this.dialogService
      .confirm('Confirm Dialog', `Are you sure you want to delete the product bundle '${productBundle.displayName}'?`)
      .pipe(filter(r => r))
      .subscribe(res => {
        this.doDeleteProductBundle(productBundle);
        this.refreshDataSource();
      });
  }

  doDeleteProductBundle(productBundle: ProductBundle) {
    this.productBundles.splice(this.productBundles.indexOf(productBundle), 1);
  }

  refreshDataSource() {
    this.tableDataSource.data = this.productBundles;
  }

  addProductBundle() {
    const newBundle = new ProductBundleViewModel();
    this.showDialog(true, ProductBundleEditorComponent, newBundle, res => {
      for (const prop in res) {
        if (res.hasOwnProperty(prop)) {
          newBundle[prop] = res[prop];
        }
      }
      this.productBundles.push(newBundle);
      this.refreshDataSource();
    });
  }

  applyFilter(filterString: string) {
    this.tableDataSource.filter = filterString.trim().toLowerCase();
  }

  showDialog(isNew: boolean, dialog: any, productBundle: ProductBundle, next) {
    this.dialog.open(dialog, {
      width: '800px',
      data: { isNew: isNew, productBundle: productBundle, next: next }
    });
  }

}
