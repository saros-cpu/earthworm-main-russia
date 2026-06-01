describe("landing navigation and authentication", () => {
  beforeEach(() => {
    cy.intercept("GET", "**/auth/csrf", {
      statusCode: 200,
      body: { headerName: "X-XSRF-TOKEN", token: "csrf-token" },
    });
    cy.intercept("GET", "**/auth/session", {
      statusCode: 200,
      body: { authenticated: false },
    });
    cy.intercept("GET", "**/admin/stats", {
      statusCode: 200,
      body: { totals: { packs: 1, courses: 1, statements: 1 } },
    });
  });

  it("opens the course library from the landing page as a guest", () => {
    cy.intercept("GET", "**/course-pack", {
      statusCode: 200,
      body: [],
    }).as("coursePacks");
    cy.visit("/");

    cy.contains("先玩一课").click();

    cy.url().should("include", "/course-pack");
    cy.wait("@coursePacks");
  });

  it("uses the browser session cookie after login", () => {
    cy.login({
      username: "acui",
      password: "yourPassword",
    });

    cy.getCookie("EW_SESSION").should("have.property", "value", "faketoken");
  });
});
