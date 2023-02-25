
#import <React/RCTViewManager.h>

@interface RCT_EXTERN_MODULE(RN3dViewManager, RCTViewManager)

RCT_EXPORT_VIEW_PROPERTY(color, NSString)
RCT_EXPORT_VIEW_PROPERTY(url, NSString)
RCT_EXPORT_VIEW_PROPERTY(loadingColor, NSString)
RCT_EXPORT_VIEW_PROPERTY(duration, NSInteger)

@end
