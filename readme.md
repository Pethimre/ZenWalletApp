# ZenWallet

### A finance tracking app in Jetpack compose that aims to let the user:
- Track their net worth by adding investment portfolios and spending wallets 
- Manage expenditures, along with future payments
- Visualize spending habits and get a summary breakdown
- Keep tabs on loans and goals
- Group your payments by categories to easily navigate through them
- Lock your app with biometrics to keep your wealth from prying eyes
- Cloud backup seamlessly for every user

### Future plans
- Add option to share payments with other users
- Have a shared pool of expenses, so you can settle expenses with your friends via the app

### Bugs to fix
- Fix loan entry save into supabase

### TODOS for later
- Implement backup and import functions
  - Support legacy ZenWallet format too
- Plan out a design for the shared payments and payment pool handling
  - Possibly creating a separate backend for it and just call the endpoint when authenticated
- Fetch news from multiple sources through existing RSS feed
  - Implement prefetch and "endless" scrolling up until a limit
- Design debt:
  - Modularization
  - Gradle build system overhaul
  - Code refactoring
  - UI overhauls
  - API call optimizations (batch calls?, caching)
  - Design an app logo and splash screen
- Maybe:
  - Animations for certain interactions
  - Store settings and configurations in supabase, instead of encrypted preferences?
  - Add tutorials for basic financial literacy materials