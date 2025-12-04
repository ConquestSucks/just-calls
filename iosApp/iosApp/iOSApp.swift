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
        // Используем объект MainViewControllerFactory
        return ComposeApp.MainViewControllerFactory.create()
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
    }
}
