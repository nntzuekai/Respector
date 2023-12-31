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

import { BaseHarnessFilters, ComponentHarness, HarnessPredicate, parallel } from '@angular/cdk/testing';

export type GioFormCardGroupHarnessFilters = BaseHarnessFilters;

export class GioFormCardGroupHarness extends ComponentHarness {
  static hostSelector = 'gio-form-card-group';

  /**
   * Gets a `HarnessPredicate` that can be used to search for a `GioFormCardGroupHarness` that meets
   * certain criteria.
   *
   * @param options Options for filtering which input instances are considered a match.
   * @return a `HarnessPredicate` configured with the given options.
   */
  static with(options: GioFormCardGroupHarnessFilters = {}): HarnessPredicate<GioFormCardGroupHarness> {
    return new HarnessPredicate(GioFormCardGroupHarness, options);
  }

  protected getCards = this.locatorForAll('gio-form-card');

  protected getCardByValue = (value: string) => this.locatorFor(`gio-form-card[ng-reflect-value="${value}"]`)();

  async getSelectedValue(): Promise<string> {
    const cards = await this.getCards();

    const cardsAttr = await parallel(() =>
      cards.map(async (row) => ({ class: await row.hasClass('selected'), value: await row.getAttribute('ng-reflect-value') })),
    );

    return cardsAttr.find((card) => card.class)?.value;
  }

  async getUnselectedValues(): Promise<string[]> {
    const cards = await this.getCards();

    const cardsAttr = await parallel(() =>
      cards.map(async (row) => ({ class: await row.hasClass('selected'), value: await row.getAttribute('value') })),
    );

    return cardsAttr.filter((card) => !card.class).map((card) => card.value);
  }

  async select(value: string): Promise<void> {
    const card = await this.getCardByValue(value);

    await card.click();
  }

  async isDisabled(): Promise<boolean> {
    const cards = await this.getCards();

    const cardsAttr = await parallel(() => cards.map(async (card) => ({ hasClass: await card.hasClass('disabled') })));

    return cardsAttr.every((card) => card.hasClass);
  }
}
