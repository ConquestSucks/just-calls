#import <Foundation/Foundation.h>

@protocol LiveKitWrapperDelegate;

@interface LiveKitWrapper : NSObject

- (instancetype)init;
- (void)createRoom;
- (void)connectWithUrl:(NSString *)url token:(NSString *)token completion:(void (^)(NSError * _Nullable))completion;
- (void)disconnectWithCompletion:(void (^)(void))completion;
- (void)setMicrophoneEnabled:(BOOL)enabled completion:(void (^)(NSError * _Nullable))completion;
- (void)setCameraEnabled:(BOOL)enabled completion:(void (^)(NSError * _Nullable))completion;
- (NSString * _Nullable)getLocalParticipantIdentity;
- (NSString * _Nullable)getLocalParticipantName;
- (BOOL)isLocalCameraEnabled;
- (BOOL)isLocalMicrophoneEnabled;
- (id _Nullable)getLocalVideoTrack;
- (NSArray<NSDictionary<NSString *, id> *> *)getRemoteParticipants;
- (id _Nullable)getRemoteVideoTrackWithParticipantId:(NSString *)participantId;
- (void)setDelegate:(id<LiveKitWrapperDelegate> _Nullable)delegate;

@end

@protocol LiveKitWrapperDelegate <NSObject>

- (void)onTrackSubscribedWithParticipantId:(NSString *)participantId isLocal:(BOOL)isLocal;
- (void)onTrackUnsubscribedWithParticipantId:(NSString *)participantId isLocal:(BOOL)isLocal;

@end

