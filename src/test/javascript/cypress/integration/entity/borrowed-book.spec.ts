import {
  entityTableSelector,
  entityDetailsButtonSelector,
  entityDetailsBackButtonSelector,
  entityCreateButtonSelector,
  entityCreateSaveButtonSelector,
  entityEditButtonSelector,
  entityDeleteButtonSelector,
  entityConfirmDeleteButtonSelector,
} from '../../support/entity';

describe('BorrowedBook e2e test', () => {
  let startingEntitiesCount = 0;

  before(() => {
    cy.window().then(win => {
      win.sessionStorage.clear();
    });
    cy.clearCookie('SESSION');
    cy.clearCookies();
    cy.intercept('GET', '/api/borrowed-books*').as('entitiesRequest');
    cy.visit('');
    cy.login('admin', 'admin');
    cy.clickOnEntityMenuItem('borrowed-book');
    cy.wait('@entitiesRequest').then(({ request, response }) => (startingEntitiesCount = response.body.length));
    cy.visit('/');
  });

  it('should load BorrowedBooks', () => {
    cy.intercept('GET', '/api/borrowed-books*').as('entitiesRequest');
    cy.visit('/');
    cy.clickOnEntityMenuItem('borrowed-book');
    cy.wait('@entitiesRequest');
    cy.getEntityHeading('BorrowedBook').should('exist');
    if (startingEntitiesCount === 0) {
      cy.get(entityTableSelector).should('not.exist');
    } else {
      cy.get(entityTableSelector).should('have.lengthOf', startingEntitiesCount);
    }
    cy.visit('/');
  });

  it('should load details BorrowedBook page', () => {
    cy.intercept('GET', '/api/borrowed-books*').as('entitiesRequest');
    cy.visit('/');
    cy.clickOnEntityMenuItem('borrowed-book');
    cy.wait('@entitiesRequest');
    if (startingEntitiesCount > 0) {
      cy.get(entityDetailsButtonSelector).first().click({ force: true });
      cy.getEntityDetailsHeading('borrowedBook');
      cy.get(entityDetailsBackButtonSelector).should('exist');
    }
    cy.visit('/');
  });

  it('should load create BorrowedBook page', () => {
    cy.intercept('GET', '/api/borrowed-books*').as('entitiesRequest');
    cy.visit('/');
    cy.clickOnEntityMenuItem('borrowed-book');
    cy.wait('@entitiesRequest');
    cy.get(entityCreateButtonSelector).click({ force: true });
    cy.getEntityCreateUpdateHeading('BorrowedBook');
    cy.get(entityCreateSaveButtonSelector).should('exist');
    cy.visit('/');
  });

  it('should load edit BorrowedBook page', () => {
    cy.intercept('GET', '/api/borrowed-books*').as('entitiesRequest');
    cy.visit('/');
    cy.clickOnEntityMenuItem('borrowed-book');
    cy.wait('@entitiesRequest');
    if (startingEntitiesCount > 0) {
      cy.get(entityEditButtonSelector).first().click({ force: true });
      cy.getEntityCreateUpdateHeading('BorrowedBook');
      cy.get(entityCreateSaveButtonSelector).should('exist');
    }
    cy.visit('/');
  });

  it('should create an instance of BorrowedBook', () => {
    cy.intercept('GET', '/api/borrowed-books*').as('entitiesRequest');
    cy.visit('/');
    cy.clickOnEntityMenuItem('borrowed-book');
    cy.wait('@entitiesRequest').then(({ request, response }) => (startingEntitiesCount = response.body.length));
    cy.get(entityCreateButtonSelector).click({ force: true });
    cy.getEntityCreateUpdateHeading('BorrowedBook');

    cy.get(`[data-cy="borrowDate"]`).type('2021-10-10').should('have.value', '2021-10-10');

    cy.setFieldSelectToLastOfEntity('book');

    cy.setFieldSelectToLastOfEntity('client');

    cy.get(entityCreateSaveButtonSelector).click({ force: true });
    cy.scrollTo('top', { ensureScrollable: false });
    cy.get(entityCreateSaveButtonSelector).should('not.exist');
    cy.intercept('GET', '/api/borrowed-books*').as('entitiesRequestAfterCreate');
    cy.visit('/');
    cy.clickOnEntityMenuItem('borrowed-book');
    cy.wait('@entitiesRequestAfterCreate');
    cy.get(entityTableSelector).should('have.lengthOf', startingEntitiesCount + 1);
    cy.visit('/');
  });

  it('should delete last instance of BorrowedBook', () => {
    cy.intercept('GET', '/api/borrowed-books*').as('entitiesRequest');
    cy.intercept('GET', '/api/borrowed-books/*').as('dialogDeleteRequest');
    cy.intercept('DELETE', '/api/borrowed-books/*').as('deleteEntityRequest');
    cy.visit('/');
    cy.clickOnEntityMenuItem('borrowed-book');
    cy.wait('@entitiesRequest').then(({ request, response }) => {
      startingEntitiesCount = response.body.length;
      if (startingEntitiesCount > 0) {
        cy.get(entityTableSelector).should('have.lengthOf', startingEntitiesCount);
        cy.get(entityDeleteButtonSelector).last().click({ force: true });
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('borrowedBook').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click({ force: true });
        cy.wait('@deleteEntityRequest');
        cy.intercept('GET', '/api/borrowed-books*').as('entitiesRequestAfterDelete');
        cy.visit('/');
        cy.clickOnEntityMenuItem('borrowed-book');
        cy.wait('@entitiesRequestAfterDelete');
        cy.get(entityTableSelector).should('have.lengthOf', startingEntitiesCount - 1);
      }
      cy.visit('/');
    });
  });
});
