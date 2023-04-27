# GommeHD.net Cheetah 1.19.4

[![Patch and Build](https://github.com/gommehdnet/cheetah-1.19/actions/workflows/build.yml/badge.svg)](https://github.com/gommehdnet/cheetah-1.19/actions/workflows/build.yml)

This is a fork of Paper tailored for the use at GommeHD.net

## How to build
- Clone this repository
- Open a shell (e.g. using Git Bash on Windows)
- On Windows, make sure that `git config core.longpaths true` is set for this project
- Run `./gradlew applyPatches` to apply the patches
- Run `./gradlew build` to build the jar files
- Run `./gradlew createReobfBundlerJar` to create the final jar file
- The final jar file can be found in `build/libs/cheetah-1.19-bundler-1.19.4-R0.1-SNAPSHOT-reobf.jar`

## How to use the API with Maven?
Currently, it is not possible to add a dependency containing NMS code, the API, and dependencies to the classpath. The current solution is to use the cheetah API combined with the normal Spigot NMS. 
Thus, changes to the NMS code will not be reflected to plugins using it.
- Run `./gradlew publishToMavenLocal` (The API artifacts are published to GommeHD.net internal repositories as well)
- Add the following maven dependency:
```
<dependency>
  <groupId>net.gommehd.cheetah</groupId>
  <artifactId>cheetah-api</artifactId>
  <version>1.19.4-R0.1-SNAPSHOT</version>
</dependency>
```

## How to apply changes
- First apply the patches using `./gradlew applyPatches`
- Perform the changes to the code
- If you are a missing file, you can import it from NMS. For this, add a new line to `build-data/dev-imports.txt`, e.g. `minecraft net.minecraft.world.level.entity.LevelEntityGetterAdapter` to import the LevelEntityGetterAdapter. Then apply the patches again (`./gradlew applyPatches`). Keep in mind that all uncommited changes to the code will get LOST by this!
- Once all changes have been applied, switch to the subproject in your shell (`cd cheetah-server` or `cd cheetah-api`) and run `git add .`. Then, commit your changes using `git commit -m "NAME OF PATCH"`
- Switch back to the project root and rebuild the patches: `./gradlew rebuildPatches`
- Commit the changed patch files the `patches/` folder to the root git repository

## Update upstream
- Make sure all changes are committed and patches have been rebuilt
- Identify the latest commit in https://github.com/PaperMC/Paper/commits/master and copy its hash. Update `paperRef` in `gradle.properties` accordingly.
- Run `./gradlew applyPatches`
- Fix potential conflicts during merges and update your code to the latest version
- Run `./gradlew rebuildPatches`
- Validate that everything works and commit your changes
- 
## Contributing
Only pull requests of the GommeHD.net dev team members are accepted. If you want to add a patch others could benefit from as well, consider submitting it to the upstream (https://github.com/PaperMC/Paper).
