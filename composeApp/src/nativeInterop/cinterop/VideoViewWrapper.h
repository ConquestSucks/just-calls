#import <UIKit/UIKit.h>

@class VideoTrack;

@interface VideoViewWrapper : UIView

- (instancetype)initWithFrame:(CGRect)frame;
- (void)setTrack:(VideoTrack * _Nullable)track;
- (void)cleanup;

@end

