import Foundation
import LiveKit

@objc public final class LiveKitWrapper: NSObject {
    private var room: Room?
    private var delegate: LiveKitWrapperDelegate?
    
    @objc public override init() {
        super.init()
    }
    
    @objc public func createRoom() {
        self.room = Room()
        if let room = self.room {
            room.add(delegate: self)
        }
    }
    
    @objc public func connectWithUrl(_ url: NSString, token: NSString, completion: @escaping (NSError?) -> Void) {
        guard let room = self.room else {
            completion(NSError(domain: "LiveKitWrapper", code: -1, userInfo: [NSLocalizedDescriptionKey: "Room not created"]))
            return
        }
        
        Task {
            do {
                try await room.connect(url: url as String, token: token as String)
                await MainActor.run {
                    completion(nil)
                }
            } catch {
                await MainActor.run {
                    completion(error as NSError)
                }
            }
        }
    }
    
    @objc public func disconnectWithCompletion(_ completion: @escaping () -> Void) {
        guard let room = self.room else {
            completion()
            return
        }
        
        Task {
            await room.disconnect()
            await MainActor.run {
                self.room = nil
                completion()
            }
        }
    }
    
    @objc public func setMicrophoneEnabled(_ enabled: Bool, completion: @escaping (NSError?) -> Void) {
        guard let room = self.room else {
            completion(NSError(domain: "LiveKitWrapper", code: -1, userInfo: [NSLocalizedDescriptionKey: "Room not connected"]))
            return
        }
        
        Task {
            do {
                try await room.localParticipant.setMicrophone(enabled: enabled)
                await MainActor.run {
                    completion(nil)
                }
            } catch {
                await MainActor.run {
                    completion(error as NSError)
                }
            }
        }
    }
    
    @objc public func setCameraEnabled(_ enabled: Bool, completion: @escaping (NSError?) -> Void) {
        guard let room = self.room else {
            completion(NSError(domain: "LiveKitWrapper", code: -1, userInfo: [NSLocalizedDescriptionKey: "Room not connected"]))
            return
        }
        
        Task {
            do {
                try await room.localParticipant.setCamera(enabled: enabled)
                await MainActor.run {
                    completion(nil)
                }
            } catch {
                await MainActor.run {
                    completion(error as NSError)
                }
            }
        }
    }
    
    @objc public func getLocalParticipantIdentity() -> String? {
        return room?.localParticipant.identity?.description
    }
    
    @objc public func getLocalParticipantName() -> String? {
        return room?.localParticipant.name
    }
    
    @objc public func isLocalCameraEnabled() -> Bool {
        return room?.localParticipant.isCameraEnabled() ?? false
    }
    
    @objc public func isLocalMicrophoneEnabled() -> Bool {
        return room?.localParticipant.isMicrophoneEnabled() ?? false
    }
    
    @objc public func getLocalVideoTrack() -> VideoTrack? {
        guard let room = self.room else { return nil }
        return room.localParticipant.mainVideoPublication?.track as? VideoTrack
    }
    
    @objc public func getRemoteParticipants() -> [[String: Any]] {
        guard let room = self.room else { return [] }
        var participants: [[String: Any]] = []
        
        for (_, participant) in room.remoteParticipants {
            var participantDict: [String: Any] = [:]
            participantDict["identity"] = participant.identity?.description ?? ""
            participantDict["name"] = participant.name ?? ""
            
            let hasVideo = participant.videoTrackPublications.values.contains { !$0.isMuted }
            let hasAudio = participant.audioTrackPublications.values.contains { !$0.isMuted }
            participantDict["isCameraEnabled"] = hasVideo
            participantDict["isMicrophoneEnabled"] = hasAudio
            
            participants.append(participantDict)
        }
        
        return participants
    }
    
    @objc public func getRemoteVideoTrack(participantId: String) -> VideoTrack? {
        guard let room = self.room else { return nil }
        
        for (_, participant) in room.remoteParticipants {
            if participant.identity?.description == participantId {
                return participant.mainVideoPublication?.track as? VideoTrack
            }
        }
        
        return nil
    }
    
    @objc public func getRemoteVideoTrackWithParticipantId(_ participantId: NSString) -> VideoTrack? {
        return getRemoteVideoTrack(participantId: participantId as String)
    }
    
    @objc public func setDelegate(_ delegate: LiveKitWrapperDelegate?) {
        self.delegate = delegate
    }
}

@objc public protocol LiveKitWrapperDelegate {
    func onTrackSubscribed(participantId: String, isLocal: Bool)
    func onTrackUnsubscribed(participantId: String, isLocal: Bool)
}

extension LiveKitWrapper: RoomDelegate {
    public func room(_ room: Room, didSubscribeTo publication: RemoteTrackPublication, participant: RemoteParticipant) {
        if publication.kind == .video {
            let participantId = participant.identity?.description ?? ""
            delegate?.onTrackSubscribed(participantId: participantId, isLocal: false)
        }
    }
    
    public func room(_ room: Room, didUnsubscribeFrom publication: RemoteTrackPublication, participant: RemoteParticipant) {
        if publication.kind == .video {
            let participantId = participant.identity?.description ?? ""
            delegate?.onTrackUnsubscribed(participantId: participantId, isLocal: false)
        }
    }
    
    public func room(_ room: Room, participant: LocalParticipant, didPublishTrack publication: LocalTrackPublication) {
        if publication.kind == .video {
            let participantId = participant.identity?.description ?? ""
            delegate?.onTrackSubscribed(participantId: participantId, isLocal: true)
        }
    }
    
    public func room(_ room: Room, participant: LocalParticipant, didUnpublishTrack publication: LocalTrackPublication) {
        if publication.kind == .video {
            let participantId = participant.identity?.description ?? ""
            delegate?.onTrackUnsubscribed(participantId: participantId, isLocal: true)
        }
    }
}

