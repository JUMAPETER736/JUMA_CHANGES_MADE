package com.uyscuti.social.circuit.presentation

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.uyscuti.social.circuit.R

data class Video(
    val title: String, val description: String, val rawResourceId: Int)

class VideoRepository(private val context: Context) {
    fun getVideos(): List<Video> {
        return listOf(
            Video("Video 1", "Lorem ipsum dolor sit amet...", R.raw.video1),
            Video("Video 2", "Cras eleifend porta ligula...", R.raw.video1),
            // Add more videos as needed
        )
    }
}

class PageViewModel : ViewModel() {

    private val _index = MutableLiveData<Int>()
    val text: LiveData<String> = _index.map { index ->
        when (index) {
            1 -> "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque faucibus gravida erat, vitae aliquet felis porta non. Curabitur tempor velit sit amet arcu convallis rutrum. Suspendisse aliquet ante ex, non luctus leo mollis non. Aliquam tristique hendrerit nisi, a vestibulum nisl. Proin tempor luctus rhoncus. Maecenas quis lorem leo. Phasellus quis velit urna. Aliquam id egestas elit. Nullam nec odio in elit iaculis volutpat. Sed erat ligula, mattis cursus velit nec, ullamcorper dapibus ante. Interdum et malesuada fames ac ante ipsum primis in faucibus. Cras vel neque vel sapien condimentum hendrerit ut vel diam. Pellentesque sed suscipit neque. Fusce et"
            2 -> "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras eleifend porta ligula, ac interdum orci elementum quis. Nulla vitae iaculis velit. Mauris non commodo eros, sit amet rutrum nisl. Sed commodo vehicula mi ac consectetur. Maecenas lacus eros, mattis ac mauris nec, mattis porttitor massa. Donec ante justo, cursus vitae turpis nec, efficitur porttitor ex. Proin rhoncus vestibulum quam, sit amet iaculis tortor. Vivamus venenatis eget felis nec ultricies.\n" +
                    "\n" +
                    "Nulla interdum justo lorem, vel consequat sapien hendrerit eu. Nam pretium id leo vitae bibendum. Nullam semper molestie orci, at faucibus magna hendrerit vel. Aenean eu mattis lacus, ut laoreet ligula. Etiam et arcu ac ante ultrices consectetur. Vivamus mauris ligula, posuere et maximus ac, blandit in nisi. Sed consequat urna consectetur imperdiet fermentum. Mauris pulvinar maximus auctor. Duis nec rutrum metus. Proin eu sapien in purus dapibus luctus eu a velit. Duis consequat a arcu in mollis.\n" +
                    "\n" +
                    "Maecenas ut orci risus. Mauris ut urna a magna consequat posuere. Mauris vel lorem id orci tincidunt semper non vel nisi. Nulla dapibus id nibh id luctus. Cras lobortis tellus a arcu consectetur varius. Aenean lorem arcu, hendrerit eget euismod ut, hendrerit sed elit. Mauris a dictum massa. Praesent mollis tincidunt arcu, id porta nunc cursus eu. Phasellus nibh augue, molestie sed nisl vitae, molestie congue ex. Integer sollicitudin, turpis vel tempor volutpat, elit augue placerat nisl, non luctus nibh orci vitae lorem. Curabitur laoreet ipsum magna, vel efficitur lectus laoreet et. Donec laoreet aliquet tincidunt.\n" +
                    "\n" +
                    "Integer commodo, nisl sit amet aliquam pellentesque, lorem lorem laoreet metus, non mattis metus dui ac tortor. Morbi viverra maximus nibh vel efficitur. Phasellus eu massa sit amet sem commodo commodo vel vitae tellus. Nam ultricies suscipit tellus ac hendrerit. Donec ac viverra orci, vitae vestibulum enim. Nunc ullamcorper volutpat risus et commodo. Praesent faucibus a dolor in pellentesque.\n" +
                    "\n" +
                    "Nulla ipsum mauris, pulvinar quis sem vitae, fermentum facilisis leo. Mauris volutpat et nisi quis lacinia. Integer lacinia efficitur urna, vitae consequat purus placerat et. Donec sit amet nisl hendrerit, molestie nisl ac, tempus nunc. Integer varius feugiat nulla, quis viverra metus pretium at. Nulla tempor placerat risus id cursus. Nulla sagittis malesuada nunc, et faucibus massa malesuada euismod. Donec non vestibulum enim. Quisque ac lacinia sapien. Integer sed eleifend nisl.\n" +
                    "\n" +
                    "Duis scelerisque nulla id mauris suscipit, sit amet facilisis velit pulvinar. Suspendisse varius volutpat nisi at mollis. Phasellus vel viverra dui, ac ornare nunc. Vestibulum tortor leo, efficitur ornare elit ac, pretium suscipit metus. Aenean condimentum feugiat dolor at congue. Vivamus risus magna, malesuada non placerat eu, scelerisque vel lectus. Aliquam et nunc gravida, dapibus magna laoreet, convallis purus. Fusce sed sem sed lacus vehicula posuere. Aenean ut libero fermentum, rutrum justo sed, rhoncus nisl.\n" +
                    "\n" +
                    "Proin sit amet elit elit. Duis finibus nisi nec urna vulputate varius. Aenean eros mi, pulvinar id mollis nec, efficitur in nisl. Donec sapien odio, efficitur in sodales vel, lobortis et lacus. Maecenas in odio purus. Sed fermentum mauris non purus efficitur vulputate. Curabitur fermentum eleifend nibh nec porta. Suspendisse lobortis velit."
            3 -> "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras eleifend porta ligula, ac interdum orci elementum quis. Nulla vitae iaculis velit. Mauris non commodo eros, sit amet rutrum nisl. Sed commodo vehicula mi ac consectetur. Maecenas lacus eros, mattis ac mauris nec, mattis porttitor massa. Donec ante justo, cursus vitae turpis nec, efficitur porttitor ex. Proin rhoncus vestibulum quam, sit amet iaculis tortor. Vivamus venenatis eget felis nec ultricies.\n" +
                    "\n" +
                    "Nulla interdum justo lorem, vel consequat sapien hendrerit eu. Nam pretium id leo vitae bibendum. Nullam semper molestie orci, at faucibus magna hendrerit vel. Aenean eu mattis lacus, ut laoreet ligula. Etiam et arcu ac ante ultrices consectetur. Vivamus mauris ligula, posuere et maximus ac, blandit in nisi. Sed consequat urna consectetur imperdiet fermentum. Mauris pulvinar maximus auctor. Duis nec rutrum metus. Proin eu sapien in purus dapibus luctus eu a velit. Duis consequat a arcu in mollis.\n" +
                    "\n" +
                    "Maecenas ut orci risus. Mauris ut urna a magna consequat posuere. Mauris vel lorem id orci tincidunt semper non vel nisi. Nulla dapibus id nibh id luctus. Cras lobortis tellus a arcu consectetur varius. Aenean lorem arcu, hendrerit eget euismod ut, hendrerit sed elit. Mauris a dictum massa. Praesent mollis tincidunt arcu, id porta nunc cursus eu. Phasellus nibh augue, molestie sed nisl vitae, molestie congue ex. Integer sollicitudin, turpis vel tempor volutpat, elit augue placerat nisl, non luctus nibh orci vitae lorem. Curabitur laoreet ipsum magna, vel efficitur lectus laoreet et. Donec laoreet aliquet tincidunt.\n" +
                    "\n" +
                    "Integer commodo, nisl sit amet aliquam pellentesque, lorem lorem laoreet metus, non mattis metus dui ac tortor. Morbi viverra maximus nibh vel efficitur. Phasellus eu massa sit amet sem commodo commodo vel vitae tellus. Nam ultricies suscipit tellus ac hendrerit. Donec ac viverra orci, vitae vestibulum enim. Nunc ullamcorper volutpat risus et commodo. Praesent faucibus a dolor in pellentesque.\n" +
                    "\n" +
                    "Nulla ipsum mauris, pulvinar quis sem vitae, fermentum facilisis leo. Mauris volutpat et nisi quis lacinia. Integer lacinia efficitur urna, vitae consequat purus placerat et. Donec sit amet nisl hendrerit, molestie nisl ac, tempus nunc. Integer varius feugiat nulla, quis viverra metus pretium at. Nulla tempor placerat risus id cursus. Nulla sagittis malesuada nunc, et faucibus massa malesuada euismod. Donec non vestibulum enim. Quisque ac lacinia sapien. Integer sed eleifend nisl.\n" +
                    "\n" +
                    "Duis scelerisque nulla id mauris suscipit, sit amet facilisis velit pulvinar. Suspendisse varius volutpat nisi at mollis. Phasellus vel viverra dui, ac ornare nunc. Vestibulum tortor leo, efficitur ornare elit ac, pretium suscipit metus. Aenean condimentum feugiat dolor at congue. Vivamus risus magna, malesuada non placerat eu, scelerisque vel lectus. Aliquam et nunc gravida, dapibus magna laoreet, convallis purus. Fusce sed sem sed lacus vehicula posuere. Aenean ut libero fermentum, rutrum justo sed, rhoncus nisl.\n" +
                    "\n" +
                    "Proin sit amet elit elit."
            else -> ""
        }
    }

    fun setIndex(index: Int) {
        _index.value = index
    }
}