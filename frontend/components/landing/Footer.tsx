import { Brain, Github, Twitter, Mail } from "lucide-react";

export const Footer = () => {
  return (
    <footer className="border-t border-border/50 bg-card/30 backdrop-blur">
      <div className="max-w-7xl mx-auto px-6 py-12">
        <div className="grid md:grid-cols-4 gap-8">
          {/* Brand */}
          <div className="space-y-4">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-lg bg-gradient-neural flex items-center justify-center">
                <Brain className="w-5 h-5 text-primary-foreground" />
              </div>
              <span className="text-xl font-bold text-card-foreground">PersonalBrain.AI</span>
            </div>
            <p className="text-muted-foreground text-sm leading-relaxed">
              Transform your scattered knowledge into an intelligent, searchable AI brain. 
              Never lose an insight again.
            </p>
          </div>

          {/* Product */}
          <div className="space-y-4">
            <h3 className="font-semibold text-card-foreground">Product</h3>
            <div className="space-y-2">
              <a href="#features" className="block text-sm text-muted-foreground hover:text-primary transition-colors">Features</a>
              <a href="#how-it-works" className="block text-sm text-muted-foreground hover:text-primary transition-colors">How it Works</a>
              <a href="#pricing" className="block text-sm text-muted-foreground hover:text-primary transition-colors">Pricing</a>
              {/* <a href="#" className="block text-sm text-muted-foreground hover:text-primary transition-colors">API Docs</a> */}
            </div>
          </div>

          {/* Company */}
          <div className="space-y-4">
            <h3 className="font-semibold text-card-foreground">Company</h3>
            <div className="space-y-2">
              <a href="#" className="block text-sm text-muted-foreground hover:text-primary transition-colors">About</a>
              <a href="#" className="block text-sm text-muted-foreground hover:text-primary transition-colors">Blog</a>
              <a href="#" className="block text-sm text-muted-foreground hover:text-primary transition-colors">Privacy</a>
              <a href="#" className="block text-sm text-muted-foreground hover:text-primary transition-colors">Terms</a>
            </div>
          </div>

          {/* Connect */}
          <div className="space-y-4">
            <h3 className="font-semibold text-card-foreground">Connect</h3>
            <div className="flex gap-3">
              <a href="#" className="w-9 h-9 rounded-lg bg-secondary hover:bg-primary hover:text-primary-foreground transition-colors flex items-center justify-center">
                <Github className="w-4 h-4" />
              </a>
              <a href="#" className="w-9 h-9 rounded-lg bg-secondary hover:bg-primary hover:text-primary-foreground transition-colors flex items-center justify-center">
                <Twitter className="w-4 h-4" />
              </a>
              <a href="#" className="w-9 h-9 rounded-lg bg-secondary hover:bg-primary hover:text-primary-foreground transition-colors flex items-center justify-center">
                <Mail className="w-4 h-4" />
              </a>
            </div>
          </div>
        </div>

        {/* Bottom */}
        <div className="border-t border-border/50 mt-12 pt-8 flex flex-col md:flex-row justify-between items-center gap-4">
          <p className="text-sm text-muted-foreground">
            © 2025 PersonalBrain.AI. All rights reserved.
          </p>
          <p className="text-sm text-muted-foreground">
            Built by Hibbaan Nawaz
          </p>
        </div>
      </div>
    </footer>
  );
};