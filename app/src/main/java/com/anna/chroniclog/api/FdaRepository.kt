package com.anna.chroniclog.api

class FdaRepository(private val fdaApi: FdaApi) {
    private val allowedProductTypes = setOf(
        "HUMAN PRESCRIPTION DRUG"
    )

    private fun extractDrugs(response: FdaApi.FdaResponse): List<FdaDrug> {
        val result = mutableListOf<FdaDrug>()
        for (item in response.results) {
            // OpenFDA sometimes has items without the 'openfda' sub-object
            item.openfda?.let { result.add(it) }
        }
        // filter our products like shampoo/sunscreen etc
        return result
            .filter { it.brandNames != null }
            .filter { drug ->
                drug.productType?.any {it in allowedProductTypes } == true
            }
            .distinctBy { it.getDisplayName() }
    }

    suspend fun searchDrugs(searchTerm: String): List<FdaDrug> {
        return try {
            // search specifically within brand names using wildcards
            val query = "openfda.brand_name:$searchTerm*"
            val response = fdaApi.searchDrugs(query)
            extractDrugs(response)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun extractReaction(response: FdaReactionResponse): List<FdaReaction> {
        return response.results
            .flatMap { it.patient.reactions } // turns List<List<Reaction>> into List<Reaction>
            .distinctBy { it.getDisplayName().uppercase() } // case-insensitive duplicate removal
    }

    suspend fun searchReaction(searchTerm: String): List<FdaReaction> {
        return try {
            val query = "patient.reaction.reactionmeddrapt:$searchTerm*"
            val response = fdaApi.searchReactions(query)
            extractReaction(response)
        } catch (e: Exception) {
            emptyList()
        }
    }
}