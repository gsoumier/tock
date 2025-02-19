/*
 * Copyright (C) 2017 VSCT
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import {saveAs} from "file-saver";
import {Component, OnInit} from "@angular/core";
import {StateService} from "../core-nlp/state.service";
import {EntityDefinition, Intent, IntentsCategory} from "../model/nlp";
import {MatDialog, MatDialogConfig, MatSnackBar, MatSnackBarConfig} from "@angular/material";
import {ConfirmDialogComponent} from "../shared-nlp/confirm-dialog/confirm-dialog.component";
import {NlpService} from "../nlp-tabs/nlp.service";
import {ApplicationService} from "../core-nlp/applications.service";
import {AddStateDialogComponent} from "./add-state/add-state-dialog.component";
import {UserRole} from "../model/auth";
import {IntentDialogComponent} from "../sentence-analysis/intent-dialog/intent-dialog.component";
import {FlatTreeControl} from "@angular/cdk/tree";
import {BehaviorSubject} from "rxjs";


@Component({
  selector: 'tock-intents',
  templateUrl: './intents.component.html',
  styleUrls: ['./intents.component.css']
})
export class IntentsComponent implements OnInit {

  UserRole = UserRole;
  treeControl: FlatTreeControl<IntentsCategory>;
  intentsCategories: BehaviorSubject<IntentsCategory[]> = new BehaviorSubject([]);
  expandedCategories: Set<string> = new Set(["default"]);

  constructor(public state: StateService,
              private nlp: NlpService,
              private snackBar: MatSnackBar,
              private dialog: MatDialog,
              private applicationService: ApplicationService) {
  }

  ngOnInit() {
    this.treeControl = new FlatTreeControl<IntentsCategory>(_ => 0, _ => true);
    this.state.currentIntentsCategories.subscribe(it => {
      this.treeControl.dataNodes = it;
      this.intentsCategories.next(it);
      it.forEach(c => {
        if (this.expandedCategories.has(c.category)) {
          this.treeControl.expand(c);
        }
      });
    })
  }

  private captureExpanded() {
    this.expandedCategories = new Set(this.intentsCategories.getValue().filter(c => this.treeControl.isExpanded(c)).map(c => c.category));
  }

  updateIntent(intent: Intent) {
    let dialogRef = this.dialog.open(
      IntentDialogComponent,
      {
        data:
          {
            name: intent.name,
            label: intent.label,
            description: intent.description,
            category: intent.category
          }
      }
    );
    dialogRef.afterClosed().subscribe(result => {
      if (result.name) {
        this.captureExpanded();
        this.nlp
          .saveIntent(
            new Intent(
              intent.name,
              this.state.user.organization,
              [],
              [this.state.currentApplication._id],
              [],
              [],
              result.label,
              result.description,
              result.category,
              intent._id)
          )
          .subscribe(intent => {
              this.state.updateIntent(intent);
            }
          )
      }
    });
  }

  deleteIntent(intent: Intent) {
    let dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: `Remove the Intent ${intent.name}`,
        subtitle: "Are you sure?",
        action: "Remove"
      }
    } as MatDialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      if (result === "remove") {
        this.captureExpanded();
        this.nlp.removeIntent(this.state.currentApplication, intent).subscribe(
          _ => {
            this.state.removeIntent(intent);
            this.snackBar.open(`Intent ${intent.name} removed`, "Remove Intent", {duration: 1000} as MatSnackBarConfig);
          },
          _ => this.snackBar.open(`Delete Intent ${intent.name} failed`, "Error", {duration: 5000} as MatSnackBarConfig)
        )
      }
    });
  }

  removeState(intent: Intent, state: string) {
    this.nlp.removeState(this.state.currentApplication, intent, state).subscribe(
      result => {
        intent.mandatoryStates.splice(intent.mandatoryStates.indexOf(state), 1);
        this.snackBar.open(`State ${state} removed from Intent ${intent.name}`, "Remove State", {duration: 1000} as MatSnackBarConfig);
      },
      _ => {
        this.snackBar.open(`Remove State failed`, "Error", {duration: 5000} as MatSnackBarConfig)
      }
    );
  }

  addState(intent: Intent) {
    let dialogRef = this.dialog.open(AddStateDialogComponent, {
      data: {
        title: `Add a state for intent \"${intent.name}\"`
      }
    } as MatDialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      if (result !== "cancel") {
        intent.mandatoryStates.push(result.name);
        this.nlp.saveIntent(intent).subscribe(
          result => {
            this.snackBar.open(`State ${result.name} added for Intent ${intent.name}`, "Add State", {duration: 1000} as MatSnackBarConfig);
          },
          _ => {
            intent.mandatoryStates.splice(intent.mandatoryStates.length - 1, 1);
            this.snackBar.open(`Add State failed`, "Error", {duration: 5000} as MatSnackBarConfig)
          }
        );
      }
    });
  }

  removeEntity(intent: Intent, entity: EntityDefinition) {
    const entityName = entity.qualifiedName(this.state.user);
    let dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: `Remove the Entity ${entityName}`,
        subtitle: "Are you sure?",
        action: "Remove"
      }
    } as MatDialogConfig);
    dialogRef.afterClosed().subscribe(result => {
      if (result === "remove") {
        this.nlp.removeEntity(this.state.currentApplication, intent, entity).subscribe(
          deleted => {
            this.state.currentApplication.intentById(intent._id).removeEntity(entity);
            if (deleted) {
              this.state.removeEntityTypeByName(entity.entityTypeName)
            }
            this.snackBar.open(`Entity ${entityName} removed from intent`, "Remove Entity", {duration: 1000} as MatSnackBarConfig);
          });
      }
    });
  }

  removeSharedIntent(intent: Intent, intentId: string) {
    this.nlp.removeSharedIntent(this.state.currentApplication, intent, intentId).subscribe(
      result => {
        intent.sharedIntents.splice(intent.sharedIntents.indexOf(intentId), 1);
        this.snackBar.open(`Shared Intent removed from Intent ${intent.name}`, "Remove Intent", {duration: 1000} as MatSnackBarConfig);
      },
      _ => {
        this.snackBar.open(`Remove Shared Intent failed`, "Error", {duration: 5000} as MatSnackBarConfig)
      }
    );
  }

  addSharedIntent(intent: Intent, intentId: string) {
    if (intent.sharedIntents.indexOf(intentId) === -1) {
      intent.sharedIntents.push(intentId);
      this.nlp.saveIntent(intent).subscribe(
        result => {
          this.snackBar.open(`Shared intent added for Intent ${intent.name}`, "Add Shared Intent", {duration: 1000} as MatSnackBarConfig);
        },
        _ => {
          intent.mandatoryStates.splice(intent.mandatoryStates.length - 1, 1);
          this.snackBar.open(`Add Shared Intent failed`, "Error", {duration: 5000} as MatSnackBarConfig)
        }
      );
    }
  }

  downloadSentencesDump(intent: Intent) {
    this.applicationService.getSentencesDumpForIntent(
      this.state.currentApplication,
      intent,
      this.state.currentLocale,
      this.state.hasRole(UserRole.technicalAdmin))
      .subscribe(blob => {
        saveAs(blob, intent.name + "_sentences.json");
        this.snackBar.open(`Dump provided`, "Dump", {duration: 1000} as MatSnackBarConfig);
      })
  }

}
