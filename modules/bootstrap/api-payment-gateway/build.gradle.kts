tasks.jar {
    enabled = false
}

tasks.bootJar {
    enabled = true
}

dependencies {
    implementation(projects.modules.domain)
    implementation(projects.modules.application)
    implementation(projects.modules.infrastructure.persistence)
    implementation(projects.modules.external.pgClient)
    implementation(libs.spring.boot.starter.jpa)
    implementation(libs.bundles.bootstrap)

    // 선택 과제: 오픈API 문서화 & 운영지표
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    implementation(libs.spring.boot.starter.actuator)

    // 선택 과제: Flyway DB 마이그레이션
    implementation(libs.flyway.core)
    implementation(libs.flyway.mysql)

    // H2 데이터베이스: 런타임 (테스트/로컬용)
    runtimeOnly(libs.database.h2)
    // MariaDB: 런타임 (프로덕션용)
    runtimeOnly(libs.database.mariadb)

    testImplementation(libs.bundles.test)
    testImplementation(libs.spring.boot.starter.test) {
        exclude(module = "mockito-core")
    }
    testImplementation(libs.spring.mockk)
}
