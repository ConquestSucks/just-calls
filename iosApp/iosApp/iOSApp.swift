import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ComposeViewController()
        }
    }
}

struct ComposeViewController: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return ComposeApp.MainViewControllerFactory.shared.create()
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
