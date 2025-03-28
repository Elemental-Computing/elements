import { AfterViewInit, Component, OnInit, ViewChild } from "@angular/core";
import { MatPaginator } from "@angular/material/paginator";
import { filter, tap } from "rxjs/operators";
import { SelectionModel } from "@angular/cdk/collections";
import { MatDialog } from "@angular/material/dialog";
import { MatTable } from "@angular/material/table";
import { AlertService } from "../../../alert.service";
import { ConfirmationDialogService } from "../../../confirmation-dialog/confirmation-dialog.service";

import { UsersService } from "../../../api/services";
import { User } from "../../../api/models";
import { CustomAuthScheme } from "../../../api/models/auth/auth-scheme-custom";
import { CustomAuthSchemesDataSource } from "../custom-auth-schemes.datasource";
import { CustomAuthSchemesService } from "../../../api/services/auth/custom-auth-schemes.service";
import { UpdateCustomAuthSchemeRequest } from "../../../api/models/auth/update-custom-auth-scheme-request";
import { CreateCustomAuthSchemeRequest } from "../../../api/models/auth/create-custom-auth-scheme-request";
import { CustomAuthSchemeDialogComponent } from "../custom-auth-scheme-dialog/custom-auth-scheme-dialog.component";
import { CustomAuthSchemeViewModel } from "../../../models/custom-auth-scheme-view-model";
import { Clipboard } from "@angular/cdk/clipboard";
import { MatSnackBar } from "@angular/material/snack-bar";

@Component({
  selector: "custom-app-auth-schemes-list",
  templateUrl: "./custom-auth-schemes-list.component.html",
  styleUrls: ["./custom-auth-schemes-list.component.css"],
})
export class CustomAuthSchemesListComponent implements OnInit, AfterViewInit {
  hasSelection = false;
  selection: SelectionModel<CustomAuthScheme>;
  dataSource: CustomAuthSchemesDataSource;

  displayedColumns: Array<string> = [
    "select",
    "audience",
    "userLevel",
    "allowedIssuers",
    "publicKey",
    "edit-action",
    "remove-action",
  ];
  currentAuthSchemes: CustomAuthScheme[];
  currentUser: User;

  @ViewChild(MatPaginator) paginator: MatPaginator;
  // @ViewChild('input') input: ElementRef;
  @ViewChild(MatTable) table: MatTable<CustomAuthScheme>;

  constructor(
    private authSchemesService: CustomAuthSchemesService,
    private alertService: AlertService,
    private dialogService: ConfirmationDialogService,
    public dialog: MatDialog,
    private clipboard: Clipboard,
    private snackBar: MatSnackBar,
    public userService: UsersService
  ) {}

  ngOnInit() {
    this.userService
      .getUser()
      .subscribe(
        (user: User) => (this.currentUser = JSON.parse(JSON.stringify(user)))
      );
    this.selection = new SelectionModel<CustomAuthScheme>(true, []);
    this.dataSource = new CustomAuthSchemesDataSource(this.authSchemesService);
    this.refresh(0);
  }

  ngAfterViewInit() {
    this.paginator.pageSize = 10;
    // server-side search
    // fromEvent(this.input.nativeElement,'keyup')
    //   .pipe(
    //     debounceTime(150),
    //     distinctUntilChanged(),
    //     tap(() => {
    //       this.paginator.pageIndex = 0;
    //       this.refresh();
    //     })
    //   )
    //   .subscribe();

    this.paginator.page.pipe(tap(() => this.refresh())).subscribe();

    this.selection.changed.subscribe(
      (s) => (this.hasSelection = this.selection.hasValue())
    );
    this.dataSource.authSchemes$.subscribe(
      (currentAuthSchemes) => (this.currentAuthSchemes = currentAuthSchemes)
    );
    this.dataSource.totalCount$.subscribe(
      (totalCount) => (this.paginator.length = totalCount)
    );
  }

  // add support for searching here
  refresh(delay = 500) {
    setTimeout(() => {
      this.selection.clear();
      this.dataSource.loadAuthSchemes(
        this.paginator.pageIndex * this.paginator.pageSize,
        this.paginator.pageSize,
        //TODO: We'll need to switch these to YOURS | SUPERUSERS | USERS
        null,
        //this.input.nativeElement.value, // for searching...
        "None"
      );
    }, delay);
  }

  /** Whether the number of selected elements matches the total number of rows. */
  isAllSelected() {
    const numSelected = this.selection.selected.length;
    const numRows = this.currentAuthSchemes.length;
    return numSelected == numRows;
  }

  /** Selects all rows if they are not all selected; otherwise clear selection. */
  masterToggle() {
    this.isAllSelected()
      ? this.selection.clear()
      : this.currentAuthSchemes.forEach((row) => this.selection.select(row));
  }

  deleteAuthScheme(authScheme) {
    this.dialogService
      .confirm(
        "Confirm Dialog",
        `Are you sure you want to remove the authScheme '${authScheme.audience}'?`
      )
      .pipe(filter((r) => r))
      .subscribe((res) => {
        this.doDeleteAuthScheme(authScheme);
        this.refresh();
      });
  }

  doDeleteAuthScheme(authScheme) {
    this.authSchemesService.deleteAuthScheme(authScheme.id).subscribe(
      (r) => {},
      (error) => this.alertService.error(error)
    );
  }

  deleteSelectedAuthSchemes() {
    this.dialogService
      .confirm(
        "Confirm Dialog",
        `Are you sure you want to remove ${
          this.selection.selected.length
        } selected auth scheme${
          this.selection.selected.length == 1 ? "" : "s"
        }?`
      )
      .pipe(filter((r) => r))
      .subscribe((res) => {
        this.selection.selected.forEach((row) => this.doDeleteAuthScheme(row));
        this.selection.clear();
        this.refresh(500);
      });
  }

  showDialog(isNew: boolean, authScheme: CustomAuthScheme, next) {
    this.dialog.open(CustomAuthSchemeDialogComponent, {
      width: "600px",
      data: {
        isNew: isNew,
        authScheme,
        next: next,
        refresher: this,
      },
    });
  }

  addAuthScheme() {
    let authSchemeViewModel = new CustomAuthSchemeViewModel();
     authSchemeViewModel.id = "";
     authSchemeViewModel.audience = "";
     authSchemeViewModel.publicKey = "";
     authSchemeViewModel.algorithm = "ECDSA_512";
     authSchemeViewModel.userLevel = "UNPRIVILEGED"
     authSchemeViewModel.tags = [];
     authSchemeViewModel.allowedIssuers = [];

    this.showDialog(
      true,
      authSchemeViewModel,
      (createTokenRequest: CreateCustomAuthSchemeRequest) => {
        return this.authSchemesService.createAuthScheme(createTokenRequest);
      }
    );
  }

  editAuthScheme(authScheme: CustomAuthScheme) {
    this.showDialog(
      false,
      authScheme,
      (updateAuthSchemeRequest: UpdateCustomAuthSchemeRequest) => {
        return this.authSchemesService.updateAuthScheme({
          id: authScheme.id,
          body: updateAuthSchemeRequest,
        });

      }
    );
  }

  copyKeyToClipboard(data: string) {
    this.clipboard.copy(data);
    this.snackBar.open("Key has been copied to clipboard.", "Dismiss", {
      duration: 3000,
    });
  }

}
