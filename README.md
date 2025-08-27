# GommeHD.net Cheetah 1.21.8

[![Patch and Build](https://github.com/gommehdnet/cheetah/actions/workflows/build.yml/badge.svg)](https://github.com/gommehdnet/cheetah/actions/workflows/build.yml)

This is a fork of Paper tailored for the use at GommeHD.net

## How to build
- Clone this repository
- Open a shell (e.g. using Git Bash on Windows)
- On Windows, make sure that `git config core.longpaths true` is set for this project
- Run `./gradlew applyAllPatches` to apply the patches
- Run `./gradlew createMojmapBundlerJar` to create the final jar file
- The final jar file can be found in `cheetah-server/build/libs/cheetah-bundler-1.21.8-R0.1-SNAPSHOT-mojmap.jar`

## How to use the API with Maven?
Currently, it is not possible to add a dependency containing NMS code, the API, and dependencies to the classpath. The current solution is to use the cheetah API combined with the normal Spigot NMS. 
Thus, changes to the NMS code will not be reflected to plugins using it.
- Run `./gradlew publishToMavenLocal` (The API artifacts are published to GommeHD.net internal repositories as well)
- Add the following maven dependency:
```
<dependency>
  <groupId>net.gommehd.cheetah</groupId>
  <artifactId>cheetah-api</artifactId>
  <version>1.21.8-R0.1-SNAPSHOT</version>
</dependency>
```

## How to apply changes
- First apply the patches using `./gradlew applyAllPatches`
- Perform the changes to the code and choose the appropriate tasks based on what you modified:
- Run `./gradlew fixupPaperApiFilePatches` and `./gradlew rebuildPaperApiFilePatches` for applying changes to paper-api files
- Run `./gradlew fixupPaperServerFilePatches` and `./gradlew rebuildServerFilePatches` for applying changes to paper-server files
- Run `./gradlew fixupMinecraftSourcePatches` and `./gradlew rebuildMinecraftSourcePatches` for applying changes to minecraft files
- Run `./gradlew rebuildPaperSingleFilePatches` for applying changes to `cheetah-api/build.gradle.kts` and `cheetah-server/build.gradle.kts` files
- Test your changes thoroughly and commit the generated patch files to the root repository

## Update upstream
- Make sure all changes are committed and patches have been rebuilt
- Identify the latest commit in https://github.com/PaperMC/Paper/commits/main/ and copy its hash. Update `paperRef` in `gradle.properties` accordingly
- Run `./gradlew applyAllPatches`
- If you run into potential conflicts, apply the rejected patches manually (See [How to apply changes](#how-to-apply-changes))
- Validate that everything works and commit your changes

## Contributing
Only pull requests of the GommeHD.net dev team members are accepted. If you want to add a patch others could benefit from as well, consider submitting it to the upstream (https://github.com/PaperMC/Paper).
