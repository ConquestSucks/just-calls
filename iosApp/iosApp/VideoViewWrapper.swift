import Foundation
import LiveKit
import SwiftUI
import UIKit

// Wrapper для отображения видео в Compose Multiplatform
@objc public class VideoViewWrapper: UIView {
    private var videoView: VideoView?
    private var track: VideoTrack?
    
    @objc public override init(frame: CGRect) {
        super.init(frame: frame)
        setupVideoView()
    }
    
    required init?(coder: NSCoder) {
        super.init(coder: coder)
        setupVideoView()
    }
    
    private func setupVideoView() {
        let videoView = VideoView()
        videoView.translatesAutoresizingMaskIntoConstraints = false
        addSubview(videoView)
        
        NSLayoutConstraint.activate([
            videoView.topAnchor.constraint(equalTo: topAnchor),
            videoView.leadingAnchor.constraint(equalTo: leadingAnchor),
            videoView.trailingAnchor.constraint(equalTo: trailingAnchor),
            videoView.bottomAnchor.constraint(equalTo: bottomAnchor)
        ])
        
        self.videoView = videoView
    }
    
    @objc public func setTrack(_ track: VideoTrack?) {
        if let oldTrack = self.track {
            videoView?.track = nil
        }
        
        self.track = track
        videoView?.track = track
    }
    
    @objc public func cleanup() {
        videoView?.track = nil
        track = nil
    }
    
    deinit {
        cleanup()
    }
}

