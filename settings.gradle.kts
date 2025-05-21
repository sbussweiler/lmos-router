// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "org.eclipse.lmos-router"
include("lmos-routing-core")
include("lmos-routing-core-spring-boot-starter")
include("lmos-routing-llm")
include("lmos-routing-llm-spring-boot-starter")
include("lmos-routing-vector")
include("lmos-routing-vector-spring-boot-starter")
include("lmos-routing-hybrid")
include("lmos-routing-hybrid-spring-boot-starter")
include("lmos-routing-workbench-demo-controller")
include("lmos-routing-workbench-demo-supervisor-agent")
