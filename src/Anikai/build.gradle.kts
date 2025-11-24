plugins {
    id("com.lagradost.cloudstream3.gradle")
}

cloudstream {
    setPackageName("com.hcgn2005.anikai")

    authors = listOf("hcgn2005-ai")

    description = "Pro-level Anikai.to provider"
    status = 1

    version = 2
}
