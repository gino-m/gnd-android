# Ground for Android
[![cloud build status](https://storage.googleapis.com/gradle_cache_bucket/status.svg)](https://console.cloud.google.com/cloud-build/dashboard?project=ground-android-gcb)

Ground is an open-source, map-first data collection and analysis platform built
to seamlessly connect the offline world with cloud-based storage and
computation. The platform consists of a web app for data management and an
Android app for offline data collection. Our goal is to provide a "just right"
data collection solution that meets the needs of community organizers,
conservationists, humanitarian workers, and researchers addressing some of
today's most pressing issues.

**Note:** Ground is not an officially supported Google product, and is developed
on a best-effort basis.

You can learn more about Ground on the [project
website](https://google.github.io/ground-platform).

## Contributing

First, read the [contribution guidelines](CONTRIBUTING.md). Ensure you
understand the code review and community guidelines and have signed the
appropriate [CLA](https://cla.developers.google.com/). 

> :exclamation: We cannot accept contributions from contributors that have not
> signed the appropriate CLA, so please be sure to sign one before submitting
> your hard work!

After you have read and understood the contribution guidelines, read the
following sections to learn how to fork this repository and contribute.

### Getting started

The following instructions describe how to fork this repository in order to
contribute to the ground-android codebase.

1. Fork this repository, see <https://help.github.com/articles/fork-a-repo/>.

2. Clone your fork:
    
    `git clone https://github.com/<username>/ground-android.git`
    
    Where `<username>` is your github username.

3. Add the base repository as a remote:
    
    `git remote add upstream https://github.com/google/ground-android.git`

4. Follow the instructions under the [Initial build
configuration](#initial-build-configuration) section of this readme to set up
your development environment.

### Developer workflow

After you have forked and cloned the repository, use the following steps to make
and manage changes. After you have finished making changes, you can submit them
to the base repository using a pull request. 

1. Pull changes from the base repository's master branch:
    
    `git pull upstream master`

1. Create a new branch to track your changes:
    
    `git checkout -b <branch>`
    
    Where `<branch>` is a meaningful name for the branch you'll use to track
    changes.

1. Make and test changes locally.

1. Run code health checks locally and fix any errors.

   1. Using Command-line:
      1. `$ ./gradlew checkCode`
    
   1. Using Android Studio
      1. Expand `gradle` side-tab (top-right corner in Android Studio IDE).
      1. Click on `Execute gradle task` button (the one with gradle logo)
      1. Type `checkCode` and press OK
      
1. Add your changes to the staging area:
    
    `git add <files>`
    
    Where `<files>` are the files you changed.
    
    > **Note:** Run `git add .` to add all currently modified files to the
    > staging area.

1. Commit your changes:
    
    `git commit -m <message>`
    
    Where `<message>` is a meaningful, short message describing the purpose of
    your changes.

1. Pull changes from the base repository's master branch, resolve conflicts if
necessary:
      
    `git pull upstream master`

1. Push your changes to your github account:
    
    `git push -u origin <branch>`
    
    Where `<branch>` is the branch name you used in step 2.

1. Create a [pull
request](https://help.github.com/articles/about-pull-requests/) to have your
changes reviewed and merged into the base repository. Reference the
[issue](https://github.com/google/ground-android/issues) your changes resolve in
either the commit message for your changes or in your pull request.
    
    > :exclamation: Any subsequent changes committed to the branch you used
    > to open your PR are automatically included in the PR. If you've opened a
    > PR but would like to continue to work on unrelated changes, be sure to
    > start a new branch to track those changes. 

    For more information on creating pull requests, see
    <https://help.github.com/articles/creating-a-pull-request/>. 
    
    To learn more about referencing issues in your pull request or commit
    messages, see
    <https://help.github.com/articles/closing-issues-using-keywords/>.

1. Celebrate!

## Initial build configuration

### Add Google Maps API Key(s)

If you do not have them, generate *release* and *debug* Google Maps API keys by
following the instructions at:

  https://developers.google.com/maps/documentation/android-api/signup.

Edit or create `gnd/secrets.properties` and set the `GOOGLE_MAPS_API_KEY` property to your API key.
```
  GOOGLE_MAPS_API_KEY=AIbzvW8e0ub...
```

In `gnd/src/debug/res/values` replace `API_KEY` with your Google Maps debug API
key. In `gnd/src/release/res/values` replace `API_KEY` with your Google Maps
release API key.

Verify the SHA-1 certificate fingerprint described in the API key generation
instructions is  registered with package name `com.google.android.gnd`. To
check, visit

  https://console.cloud.google.com/apis/credentials

To view the SHA-1 of the debug key generated by Android Studio run:

``` 
$ keytool -list -v \
    -keystore "$HOME/.android/debug.keystore" \ 
    -alias androiddebugkey \ 
    -storepass android -keypass android 
```

### Set up Firebase

1. Create a new Firebase project at:

    https://console.firebase.google.com/

2. Save config file for Android app to `gnd/src/debug/google-services.json`:

    https://support.google.com/firebase/answer/7015592

This includes the API key and URL for your new Firebase project.

### Set up Google Cloud Build (optional)

Used to build with Google Cloud and for running integration tests:

1. Install google-cloud-sdk

2. gcloud init
 
3. gcloud auth login
  
4. gcloud config set project [PROJECT_ID]

### Troubleshooting

1. App crashes on start with following stacktrace:
 
    ```
    java.lang.RuntimeException: Unable to get provider com.google.firebase.provider.FirebaseInitProvider: java.lang.IllegalArgumentException: Given String is empty or null
    ```
    
   Solution: Ensure `gnd/src/debug/google-services.json` exists and is valid, as per instructions in [Set up Firebase](#set-up-firebase).
