#import <UIKit/UIKit.h>

@interface VideoViewWrapper : UIView

- (instancetype)initWithFrame:(CGRect)frame;
- (void)setTrack:(id _Nullable)track;
- (void)cleanup;

