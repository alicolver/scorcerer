/**
 * ScorePredictor
 * An API for the Score Predictor Backend
 *
 * The version of the OpenAPI document: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
*/
package scorcerer.server.models

import se.ansman.kotshi.JsonSerializable

@JsonSerializable
data class User (
    val name: String,
    val userId: String
)

