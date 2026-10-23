# Antigravity Project Rules — Java Mail Client

## 1. Project Context

Project: Java Mail Client Desktop

Technology:
- Java
- Java Swing + FlatLaf Light + MigLayout
- Maven
- SMTP, POP3/IMAP
- Socket/Stream, TLS/SSL, MIME

Primary specification: `PROJECT_SPEC.md`

The project specification is the source of truth for requirements. Do not invent additional business requirements.

## 2. Context Optimization

Minimize unnecessary file reading and token usage.

Before starting a task:
1. Identify the user's requested functionality.
2. Read `PROJECT_MAP.md` if it exists.
3. Locate relevant classes and dependencies.
4. Read only the necessary files.
5. Expand the scope only when required.

Do not scan the entire repository by default.

Ignore unrelated generated files, build outputs, logs and dependencies.

Do not repeatedly read unchanged files within the same task.

## 3. Code Modification Rules

- Preserve existing business logic.
- Do not modify unrelated modules.
- Do not rewrite working code without a justified reason.
- Follow the existing package structure.
- Avoid unnecessary dependencies.
- Keep changes minimal, maintainable and testable.
- Do not hardcode credentials or sensitive configuration.

## 4. Swing Rules

- Use Java Swing + FlatLaf Light as the primary desktop UI framework.
- Use MigLayout or standard Swing layout managers.
- Separate View, Service and Model responsibilities.
- Do not execute network operations on the Event Dispatch Thread (EDT).
- Use SwingWorker for background operations.
- Keep UI responsive during SMTP/IMAP operations.

## 5. UI/UX Rules

Follow the Cloud Blue Design System defined in `PROJECT_SPEC.md`.

Design principles:
- White-dominant interface (#FFFFFF).
- Minimalism and professional desktop appearance.
- Consistent typography, spacing and icons.
- No unnecessary gradients, decorations or animations.
- No fake buttons or placeholder business functionality.
- Prioritize usability over visual effects.

## 6. Business Logic Rules

- Follow the project specification and course requirements.
- Preserve correct SMTP, POP3/IMAP and TLS behavior.
- Do not replace Socket-based implementations without checking the specification.
- Keep network protocol logic separate from UI.
- Handle errors and sensitive information securely.

## 7. Testing Rules

After modifying code:
1. Check compilation.
2. Run relevant tests.
3. Verify affected functionality.
4. Check for regressions.
5. Report what was tested and what remains unverified.

Never claim successful testing without actually performing it.

## 8. Documentation Rules

- Keep `PROJECT_MAP.md` updated when file structure or module responsibilities change.
- Do not rewrite the entire documentation for small changes.
- Avoid duplicating detailed requirements from `PROJECT_SPEC.md`.

## 9. Task Execution Rules

For each task:
1. Understand the request.
2. Identify relevant files.
3. Make focused changes.
4. Verify results.
5. Summarize modified files and test outcomes.

Do not perform unrelated refactoring or introduce features outside the requested scope.

## 10. Communication

- Respond in Vietnamese.
- Keep explanations concise and technical.
- Explain important architectural decisions.
- Clearly distinguish completed work from proposed work.
- Ask for confirmation before destructive or major architectural changes.