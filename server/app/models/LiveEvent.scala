package models

case class LiveEvent(
    name: String,
    place: String,
    date: String,
    description: String,
    image: String = "/assets/images/placeholder/318x180.png"
)
