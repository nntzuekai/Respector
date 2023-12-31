/*
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { GioIconsModule } from '@gravitee/ui-particles-angular';
import { MatTableModule } from '@angular/material/table';
import { MatSortModule } from '@angular/material/sort';

import { InstanceDetailsEnvironmentComponent } from './instance-details-environment.component';

import { GioTableWrapperModule } from '../../../../shared/components/gio-table-wrapper/gio-table-wrapper.module';
import { GioTableOfContentsModule } from '../../../../shared/components/gio-table-of-contents/gio-table-of-contents.module';

@NgModule({
  declarations: [InstanceDetailsEnvironmentComponent],
  exports: [InstanceDetailsEnvironmentComponent],
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    GioIconsModule,
    GioTableWrapperModule,
    MatTableModule,
    GioTableOfContentsModule,
    MatSortModule,
  ],
})
export class InstanceDetailsEnvironmentModule {}
