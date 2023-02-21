#import <React/RCTViewManager.h>

@interface RCT_EXTERN_MODULE(ModelViewerViewManager, RCTViewManager)

RCT_EXPORT_VIEW_PROPERTY(color, NSString)
RCT_EXPORT_VIEW_PROPERTY(url, NSString)
RCT_EXPORT_VIEW_PROPERTY(loadingColor, NSString)

@end
