package com.anna.chroniclog.api

class FdaRepository(private val FdaApi: FdaApi) {

    private fun extractDrugs(response: FdaApi.FdaResponse): List<FdaDrug> {
        val result = mutableListOf<FdaDrug>()
        for (item in response.results) {
            // OpenFDA sometimes has items without the 'openfda' sub-object
            item.openfda?.let { result.add(it) }
        }
        // Filter to ensure we have a name and remove duplicates
        return result.filter { it.brandNames != null }.distinctBy { it.getDisplayName() }
    }

    suspend fun searchDrugs(searchTerm: String): List<FdaDrug> {
        return try {
            // Search specifically within brand names using wildcards
            val query = "openfda.brand_name:$searchTerm*"
            val response = FdaApi.searchDrugs(query)
            extractDrugs(response)
        } catch (e: Exception) {
            emptyList()
        }
    }
}