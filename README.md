# CCG-Example
Google Ads Custom Click Gesture Example App

## Without Custom click gesture

https://user-images.githubusercontent.com/102366289/171194209-809f0181-6c15-47eb-8ebb-b74ea6e0a291.mp4

## With Custom click gesture

https://user-images.githubusercontent.com/102366289/171194283-3c90da46-f0f0-450b-b733-6efd4f4660db.mp4

NativeAd.recordCustomClickGesture() invoked when:
1. NativeAdView.mediaView is clicked
2. NativeAdView.callToActionView is clicked
3. NativeAdView is swiped left (need to invoke in touch event of the viewgroup that forwards the onInterceptTouchEvent)

**Note:** *Swipe needs to initiate on either NativeAdView or its elements*

https://user-images.githubusercontent.com/102366289/172149869-a9b870d5-318a-49cc-8f47-67a334de80f0.mp4

Everything works fine. Ad-click is imitated

NativeAd.recordCustomClickGesture() invoked when:
1. Button inside NativeAdView is clicked (not part of NativeAdView elements)
2. Button outside NativeAdView is clicked

Each of them does not imitate ad-click.
Logcat shows following message:
```
I/Ads: Received log message: <Google:HTML> Custom clicks must be reported immediately after they are detected.
```


## Badoo App

![Webp net-resizeimage](https://user-images.githubusercontent.com/102366289/171196320-79c1b2ce-47ca-4efa-a692-2fac0f3b0efc.png)
![Webp net-resizeimage (1)](https://user-images.githubusercontent.com/102366289/171196349-bb451486-68f4-412c-8e08-cdc8a4cf4312.png)
