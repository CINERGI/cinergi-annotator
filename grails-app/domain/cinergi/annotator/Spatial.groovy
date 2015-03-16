package cinergi.annotator

class Spatial {
    List<BoundingBoxRec> boundingBoxes
    List<BoundingBoxRec> derivedBoundingBoxesFromPlaces
    Map<String, BoundingBoxRec> derivedBoundingBoxesFromDerivedPlace
    List<String> placeKeywords
    String text
    List<String> invalidPlaceKeywords
    List<String> derivedPlaceFromText

    def toMap() {
        def dpbbMap = [:]
        derivedBoundingBoxesFromDerivedPlace.each { k, BoundingBoxRec v ->
            dpbbMap[k] = v.toMap()
        }
        return ['bounding_boxes'                           : boundingBoxes.collect { BoundingBoxRec b -> b.toMap() },
                'place_keywords'                           : placeKeywords, 'text': text,
                'invalid_place_keywords'                   : invalidPlaceKeywords,
                'derived_bounding_boxes_from_places'       : derivedBoundingBoxesFromPlaces.collect { BoundingBoxRec b -> b.toMap() },
                'derived_place_from_text'                  : derivedPlaceFromText,
                'derived_bounding_boxes_from_derived_place': dpbbMap,

        ]
    }
    static embedded = ['boundingBoxes', 'derivedBoundingBoxesFromPlaces', 'derivedBoundingBoxesFromDerivedPlace']
    static mapping = {
        version false
        boundingBoxes attr: 'bounding_boxes'
        derivedBoundingBoxesFromPlaces attr: 'derived_bounding_boxes_from_places'
        derivedBoundingBoxesFromDerivedPlace attr: 'derived_bounding_boxes_from_derived_place'
        placeKeywords attr: 'place_keywords'
        invalidPlaceKeywords attr: 'invalid_place_keywords'
        derivedPlaceFromText attr: 'derived_place_from_text'

    }
}
