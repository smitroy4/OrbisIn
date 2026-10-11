export function Terms() {
  return (
    <div className="page">
      <div className="singlecol">
        <div className="card cardpad">
          <h3>Terms of Service</h3>
          <p className="note">Last updated: October 2026.</p>
          <h4>1. What OrbisIn is</h4>
          <p>
            OrbisIn is a demonstration professional network. Accounts,
            posts, connections, and alerts are stored across several
            backing services and shown here in one interface.
          </p>
          <h4>2. Your account</h4>
          <p>
            You are responsible for keeping your password private.
            Accounts created for testing may be removed at any time by
            the operator.
          </p>
          <h4>3. Your content</h4>
          <p>
            Posts you publish are visible to members connected to the
            network. Do not publish anything unlawful, confidential, or
            abusive. The operator may remove content that breaks these
            rules.
          </p>
          <h4>4. Availability</h4>
          <p>
            The service is provided as is, without warranties. Parts of
            the system may be paused or reset during development, which
            can temporarily hide posts, connections, or alerts.
          </p>
          <h4>5. Contact</h4>
          <p>
            Questions about these terms can be sent to the project
            operator through the repository issue tracker.
          </p>
        </div>
      </div>
    </div>
  );
}

export function Privacy() {
  return (
    <div className="page">
      <div className="singlecol">
        <div className="card cardpad">
          <h3>Privacy Policy</h3>
          <p className="note">Last updated: October 2026.</p>
          <h4>1. Data we store</h4>
          <p>
            We store your name, email address, and a salted hash of
            your password. Passwords themselves are never stored. We
            also store the posts you publish, your connections, and the
            alerts generated for you.
          </p>
          <h4>2. How sessions work</h4>
          <p>
            Signing in issues a signed token kept in your browser. The
            token identifies you to the API gateway and can be removed
            at any time by signing out, which clears it from the
            browser.
          </p>
          <h4>3. Sharing</h4>
          <p>
            Your name and posts are visible to other members of the
            network. We do not sell personal data and we do not use
            third party advertising trackers on this site.
          </p>
          <h4>4. Retention and removal</h4>
          <p>
            Content lives in the backing databases until removed by you
            or by the operator. Because this is a demo system,
            databases may be reset, which deletes all stored data.
          </p>
        </div>
      </div>
    </div>
  );
}
