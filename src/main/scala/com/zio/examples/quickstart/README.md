## Eat chocolate with ZIO
This app interacts with the `Console` and asks the user to enter the number of the chocolate and eat them one by one
the maximum chocolate to eat is 10, and the duration of eating is 1 second/chocolate 
If the input is not valid the program retries (maximum 2 trials) 

![](https://emojipedia-us.s3.dualstack.us-west-1.amazonaws.com/thumbs/240/apple/237/chocolate-bar_1f36b.png)

## ZLayers by example
This example uses a service `Show` with a method `display` that could have different implementations.
ZLayer is a constructor that contains the implementation of the required service.
In this example, there are 3 different implementations == 3 ZLayers.

To understand this example step by step, check out this coding session video: [ZLayers by example](https://youtu.be/u5IrfkAo6nk)