package proximedia.com.au.sparxkotlin.models

import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * Created by pc on 16/10/2017.
 */

@IgnoreExtraProperties
class Profile {

    var title: String? = null
    var description: String? = null
    var category: String? = null
    var photo: String? = null

    constructor() {}

    constructor(title: String, description: String, category: String, photo: String) {
        this.title = title
        this.description = description
        this.category = category
        this.photo = photo
    }

    companion object {

        val FIELD_CITY = "city"
        val FIELD_CATEGORY = "category"
        val FIELD_PRICE = "price"
        val FIELD_POPULARITY = "numRatings"
        val FIELD_AVG_RATING = "avgRating"
    }
}