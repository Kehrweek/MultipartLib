# Multipart Lib

[![Fabric](https://img.shields.io/static/v1?logo=fabric&label=Fabric&message=1.19.2&color=informational)]()
[![Modrinth](https://img.shields.io/static/v1?label=Modrinth&message=+&color=1bd96a)](https://modrinth.com/mods)
[![CurseForge](https://img.shields.io/static/v1?label=CurseForge&message=+&color=f16436)](https://modrinth.com/mods)
[![Discord](https://img.shields.io/discord/1114547799556620359?logoColor=white&color=5865f2&label=Discord&logo=discord)](https://discord.gg/2T765VZc)
[![GitHub](https://img.shields.io/github/license/Kehrweek/MultipartLib)](https://github.com/Kehrweek/MultipartLib/blob/main/LICENSE)

[<img src="https://i.imgur.com/Ol1Tcf8.png" width="150px"/>](https://fabricmc.net)

MultipartLib is a free and open-source library for Minecraft that enables
developers to create blocks consisting of different parts.

:warning: MultipartLib is still in alpha, please report any issues you
encounter.
Feature requests are welcome too.

---

## Do I need this?

For players, this library is only necessary if a mod requires it as a
dependency.
However, for developers, this library is useful if they want to create blocks
with different parts,
surpassing Minecraft's limitations of predefined BlockStates.
With this API, you can implement Parts that have different PartStates and
individual PartEntities.

---

## Reporting Issues

You can report bugs and crashes by opening an issue on our issue tracker.
Before opening a new issue, use the search tool to make sure that your issue
has not already been
reported<!-- TODO and ensure that you have completely filled out the issue template-->.
Issues that are duplicates or do not contain the necessary information to
triage and debug may be closed.

---

## Developer Information

![Build](https://github.com/Kehrweek/Multipartlib/actions/workflows/build.yml/badge.svg?branch=main)

:warning: The Wiki might not be complete, because this library is still in
alpha.
If you have any questions about this library, please join the discord.

### Dependency

[![Latest version](https://api-prd.cloudsmith.io/v1/badges/version/kehrweek-packages/multipartlib/maven/multipartlib/latest/a=noarch;xg=de.kehrweek/?render=true&show_latest=true&label=Cloudsmith&style=flat&labelColor=+)](https://cloudsmith.io/~kehrweek-packages/repos/multipartlib/packages/)

For development package distribution, Cloudsmith is used.
You can find the latest package there.
For experimental builds use `-SNAPSHOT` in the version.

```groovy
repositories {
    maven {
        name = "MultipartLib"
        url = "https://dl.cloudsmith.io/public/kehrweek-packages/multipartlib/maven/"
    }
}
dependencies {
    modImplementation "de.kehrweek:multipartlib:<VERSION>"
}
```

### Community

We have an official Discord community to help you when creating a mod using our
library.
Feel free to ask any questions.

---