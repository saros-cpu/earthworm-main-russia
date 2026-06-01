/// <reference types="cypress" />
// ***********************************************
// This example commands.ts shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
Cypress.Commands.add("login", function ({ username, password }: { username: string; password: string }) {
  const user = {
    userId: "user-1",
    username: "acui",
    nickname: "acui",
    role: "USER",
  };

  cy.intercept("POST", "**/auth/login", (req) => {
    expect(req.body.username).to.eq(username);
    expect(req.body.password).to.eq(password);

    req.reply({
      statusCode: 200,
      headers: {
        "set-cookie": "EW_SESSION=faketoken; Path=/; HttpOnly; SameSite=Strict",
      },
      body: user,
    });
  }).as("login");

  cy.visit("/login");

  cy.get('input[type="text"]').first().type(username);
  cy.get('input[type="password"]').type(`${password}{enter}`);
  cy.wait("@login");

  cy.getCookie("EW_SESSION").should("have.property", "value", "faketoken");

  // Login navigates back to the landing page after the session cookie is issued.
  cy.url().should("eq", Cypress.config("baseUrl"));
});
//
//
// -- This is a child command --
// Cypress.Commands.add('drag', { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add('dismiss', { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite('visit', (originalFn, url, options) => { ... })
//
declare global {
  namespace Cypress {
    interface Chainable {
      login(params: { username: string; password: string }): Chainable<void>;
      //       drag(subject: string, options?: Partial<TypeOptions>): Chainable<Element>
      //       dismiss(subject: string, options?: Partial<TypeOptions>): Chainable<Element>
      //       visit(originalFn: CommandOriginalFn, url: string, options: Partial<VisitOptions>): Chainable<Element>
    }
  }
}

export {};
